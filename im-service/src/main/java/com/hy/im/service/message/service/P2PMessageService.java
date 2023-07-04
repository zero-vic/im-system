package com.hy.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.hy.im.codec.pack.message.ChatMessageAck;
import com.hy.im.codec.pack.message.MessageReciveServerAckPack;
import com.hy.im.common.config.AppConfig;
import com.hy.im.common.constant.CallbackCommand;
import com.hy.im.common.constant.SeqConstants;
import com.hy.im.common.enums.ConversationTypeEnum;
import com.hy.im.common.enums.command.MessageCommand;
import com.hy.im.common.model.ClientInfo;
import com.hy.im.common.model.message.MessageContent;
import com.hy.im.common.model.message.OfflineMessageContent;
import com.hy.im.common.response.ResponseVO;
import com.hy.im.service.message.model.req.SendMessageReq;
import com.hy.im.service.message.model.resp.SendMessageResp;
import com.hy.im.service.seq.RedisSeq;
import com.hy.im.service.util.CallbackService;
import com.hy.im.service.util.ConversationIdGenerate;
import com.hy.im.service.util.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName P2PMessageService
 * description: 单聊消息处理
 * yao create 2023年07月03日
 * version: 1.0
 */
@Service
public class P2PMessageService {
    private final static Logger log = LoggerFactory.getLogger(P2PMessageService.class);

    @Autowired
    private CheckSendMessageService checkSendMessageService;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private MessageStoreService messageStoreService;

    @Autowired
    private RedisSeq redisSeq;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private CallbackService callbackService;

    /**
     * 私有线程池
     */
    private final ThreadPoolExecutor threadPoolExecutor;

    {
        final AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("message-process-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }

    /**
     *
     * @param messageContent
     */
    public void process(MessageContent messageContent){
        log.info("消息开始处理：{}",messageContent.getMessageId());

        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();
        String messageId = messageContent.getMessageId();
        // 用messageId 从缓存中拿消息
        MessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache(appId, messageId, MessageContent.class);
        if(messageFromMessageIdCache!=null){
            // 不需要持久化 ，只需要消息分发
            threadPoolExecutor.execute(() ->{
                ack(messageContent,ResponseVO.successResponse());
                //2.发消息给同步在线端
                syncToSender(messageFromMessageIdCache,messageFromMessageIdCache);
                //3.发消息给对方在线端
                List<ClientInfo> clientInfos = dispatchMessage(messageFromMessageIdCache);
                if(clientInfos.isEmpty()){
                    //发送接收确认给发送方，要带上是服务端发送的标识
                    reciverAck(messageFromMessageIdCache);
                }
            });
            return;
        }
        //回调
        ResponseVO responseVO = ResponseVO.successResponse();
        if(appConfig.isSendMessageAfterCallback()){
            responseVO = callbackService.beforeCallback(messageContent.getAppId(), CallbackCommand.SEND_MESSAGE_BEFORE
                    , JSONObject.toJSONString(messageContent));
        }

        if(!responseVO.isOk()){
            ack(messageContent,responseVO);
            return;
        }

        //appId + Seq + (from + to) groupId
        long seq = redisSeq.doGetSeq(messageContent.getAppId() + ":"
                + SeqConstants.MESSAGE+ ":" + ConversationIdGenerate.generateP2PId(
                messageContent.getFromId(),messageContent.getToId()
        ));
        messageContent.setMessageSequence(seq);
        /**
         * 前置校验 （是否禁言、禁用，是否是好友关系）
         */
//        ResponseVO responseVO = imServerPermissionCheck(fromId, toId, appId);
//        if(responseVO.isOk()){
//            threadPoolExecutor.execute(() -> {
//                //msg持久化
//                messageStoreService.storeP2PMessage(messageContent);
//                //1.回ack成功给自己
//                ack(messageContent,responseVO);
//                //2.发消息给同步在线端
//                syncToSender(messageContent,messageContent);
//                //3.发消息给对方在线端
//                dispatchMessage(messageContent);
//            });
//
//
//        }else {
//            // 告诉客户端失败了
//            ack(messageContent,responseVO);
//        }
        // 前置校验提到了tcp中
        threadPoolExecutor.execute(() -> {
            //msg持久化
            messageStoreService.storeP2PMessage(messageContent);
            // 插入离线消息
            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(messageContent,offlineMessageContent);
            offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
            messageStoreService.storeOfflineMessage(offlineMessageContent);
            //1.回ack成功给自己
            ack(messageContent,ResponseVO.successResponse());
            //2.发消息给同步在线端
            syncToSender(messageContent,messageContent);
            //3.发消息给对方在线端
            List<ClientInfo> clientInfos = dispatchMessage(messageContent);
            messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(),
                    messageContent.getMessageId(),messageContent);
            if(CollectionUtils.isEmpty(clientInfos)){
                reciverAck(messageContent);
            }

            if(appConfig.isSendMessageAfterCallback()){
                callbackService.callback(messageContent.getAppId(),CallbackCommand.SEND_MESSAGE_AFTER,
                        JSONObject.toJSONString(messageContent));
            }

            log.info("消息处理完成：{}",messageContent.getMessageId());
        });


    }


    /**
     * 校验是否禁言、禁用，是否是好友关系
     * @param fromId
     * @param toId
     * @param appId
     * @return
     */
    public ResponseVO imServerPermissionCheck(String fromId,String toId,Integer appId){
        ResponseVO responseVO = checkSendMessageService.checkSenderForvidAndMute(fromId, appId);
        if(!responseVO.isOk()){
            return responseVO;
        }
        return checkSendMessageService.checkFriendShip(fromId, toId, appId);
    }

    /**
     * 发送消息（管理员）
     * @param req
     * @return
     */
    public SendMessageResp send(SendMessageReq req) {

        SendMessageResp sendMessageResp = new SendMessageResp();
        MessageContent message = new MessageContent();
        BeanUtils.copyProperties(req,message);
        //插入数据
        messageStoreService.storeP2PMessage(message);
        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());

        //2.发消息给同步在线端
        syncToSender(message,message);
        //3.发消息给对方在线端
        dispatchMessage(message);
        return sendMessageResp;
    }

    /**
     * 处理ack
     * @param messageContent
     * @param responseVO
     */
    private void ack(MessageContent messageContent,ResponseVO responseVO){
        log.info("msg ack,msgId={},checkResut{}",messageContent.getMessageId(),responseVO.getCode());
        ChatMessageAck chatMessageAck = new
                ChatMessageAck(messageContent.getMessageId(),messageContent.getMessageSequence());
        responseVO.setData(chatMessageAck);
        //发消息
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK,
                responseVO,messageContent
        );
    }

    /**
     * 接受确认ack 服务端使用
     * @param messageContent
     */
    public void reciverAck(MessageContent messageContent){
        MessageReciveServerAckPack pack = new MessageReciveServerAckPack();
        pack.setFromId(messageContent.getToId());
        pack.setToId(messageContent.getFromId());
        pack.setMessageKey(messageContent.getMessageKey());
        pack.setMessageSequence(messageContent.getMessageSequence());
        pack.setServerSend(true);
        messageProducer.sendToUser(messageContent.getFromId(),MessageCommand.MSG_RECIVE_ACK,
                pack,new ClientInfo(messageContent.getAppId(),messageContent.getClientType()
                        ,messageContent.getImei()));
    }

    /**
     * 发消息给同步在线端
     * @param messageContent
     * @param clientInfo
     */
    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo){
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P,messageContent,messageContent);
    }

    /**
     * 发消息给对方在线端
     * @param messageContent
     * @return
     */
    private List<ClientInfo> dispatchMessage(MessageContent messageContent){
        List<ClientInfo> clientInfos = messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_P2P,
                messageContent, messageContent.getAppId());
        return clientInfos;
    }

//    /**
//     * 发消息给对方在线端
//     * @param messageContent
//     */
//    private void dispatchMessage(MessageContent messageContent){
//        messageProducer.sendToUser(messageContent.getToId(),MessageCommand.MSG_P2P,
//                messageContent,messageContent.getAppId());
//    }
}
