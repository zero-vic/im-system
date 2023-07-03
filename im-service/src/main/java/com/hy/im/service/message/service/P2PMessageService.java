package com.hy.im.service.message.service;

import com.hy.im.codec.pack.message.ChatMessageAck;
import com.hy.im.common.enums.command.MessageCommand;
import com.hy.im.common.model.ClientInfo;
import com.hy.im.common.model.message.MessageContent;
import com.hy.im.common.response.ResponseVO;
import com.hy.im.service.util.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     *
     * @param messageContent
     */
    public void process(MessageContent messageContent){
        log.info("消息开始处理：{}",messageContent.getMessageId());

        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();

        /**
         * 前置校验 （是否禁言、禁用，是否是好友关系）
         */
        ResponseVO responseVO = imServerPermissionCheck(fromId, toId, appId);
        if(responseVO.isOk()){
            //1.回ack成功给自己
            ack(messageContent,responseVO);
            //2.发消息给同步在线端
            syncToSender(messageContent,messageContent);
            //3.发消息给对方在线端
            dispatchMessage(messageContent);
        }else {
            // 告诉客户端失败了
            ack(messageContent,responseVO);
        }


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
     * 发消息给同步在线端
     * @param messageContent
     * @param clientInfo
     */
    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo){
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P,messageContent,messageContent);
    }

//    /**
//     *
//     * @param messageContent
//     * @return
//     */
//    private List<ClientInfo> dispatchMessage(MessageContent messageContent){
//        List<ClientInfo> clientInfos = messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_P2P,
//                messageContent, messageContent.getAppId());
//        return clientInfos;
//    }

    /**
     * 发消息给对方在线端
     * @param messageContent
     */
    private void dispatchMessage(MessageContent messageContent){
        messageProducer.sendToUser(messageContent.getToId(),MessageCommand.MSG_P2P,
                messageContent,messageContent.getAppId());
    }
}
