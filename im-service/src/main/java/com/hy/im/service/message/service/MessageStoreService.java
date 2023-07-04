package com.hy.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.hy.im.common.config.AppConfig;
import com.hy.im.common.constant.RabbitConstants;
import com.hy.im.common.constant.RedisConstants;
import com.hy.im.common.enums.ConversationTypeEnum;
import com.hy.im.common.enums.DelFlagEnum;
import com.hy.im.common.model.message.*;
import com.hy.im.service.conversation.service.ConversationService;
import com.hy.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.hy.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.hy.im.service.message.dao.ImMessageBodyEntity;
import com.hy.im.service.message.dao.ImMessageHistoryEntity;
import com.hy.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.hy.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.hy.im.service.util.SnowflakeIdWorker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName MessageStoreService
 * description: 消息持久化服务
 * yao create 2023年07月04日
 * version: 1.0
 */
@Service
public class MessageStoreService {
    private final static Logger log = LoggerFactory.getLogger(MessageStoreService.class);

    @Autowired
    private ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    private ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    private ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Autowired
    private AppConfig appConfig;
    /**
     * 单聊消息持久化
     * @param messageContent
     */
    @Transactional(rollbackFor = Exception.class)
    public void storeP2PMessage(MessageContent messageContent){
        // 转换messageBody
//        ImMessageBody imMessageBody = extractMessageBody(messageContent);
//        imMessageBodyMapper.insert(imMessageBody);
//        // 转换messageHistory
//        List<ImMessageHistory> imMessageHistories = extractToP2PMessageHistory(messageContent, imMessageBody);
//        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistories);
//        messageContent.setMessageKey(imMessageBody.getMessageKey());
        // 使用mq异步处理
        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
        ImMessageBody imMessageBody = new ImMessageBody();
        BeanUtils.copyProperties(imMessageBodyEntity,imMessageBody);
        DoStoreP2PMessageDto dto = new DoStoreP2PMessageDto();
        dto.setMessageContent(messageContent);
        dto.setMessageBody(imMessageBody);
        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
        rabbitTemplate.convertAndSend(RabbitConstants.STOREP_2_PMESSAGE,"",
                JSONObject.toJSONString(dto));
    }

    /**
     * 将 messageContent 转换成 messagebody
     * @param messageContent
     * @return
     */
    public ImMessageBodyEntity extractMessageBody(MessageContent messageContent){
        ImMessageBodyEntity imMessageBodyEntity = new ImMessageBodyEntity();
        imMessageBodyEntity.setAppId(messageContent.getAppId());
        imMessageBodyEntity.setMessageKey(SnowflakeIdWorker.nextId());
        imMessageBodyEntity.setMessageTime(messageContent.getMessageTime());
        imMessageBodyEntity.setMessageBody(messageContent.getMessageBody());
        imMessageBodyEntity.setCreateTime(System.currentTimeMillis());
        imMessageBodyEntity.setSecurityKey("");
        imMessageBodyEntity.setExtra(messageContent.getExtra());
        imMessageBodyEntity.setDelFlag(DelFlagEnum.NORMAL.getCode());
        return imMessageBodyEntity;
    }

    /**
     * 把messageContent 转换成 messageHistory
     * @param messageContent
     * @param imMessageBodyEntity
     * @return
     */
    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent,
                                                                   ImMessageBodyEntity imMessageBodyEntity){
        List<ImMessageHistoryEntity> list = new ArrayList<>();
        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());

        list.add(fromHistory);
        list.add(toHistory);
        return list;

    }

    /**
     * 群聊消息持久化
     * @param messageContent
     */
    @Transactional(rollbackFor = Exception.class)
    public void storeGroupMessage(GroupChatMessageContent messageContent){
//        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
//        imMessageBodyMapper.insert(imMessageBodyEntity);
//        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, imMessageBodyEntity);
//        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
//        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
        ImMessageBody imMessageBody = new ImMessageBody();
        BeanUtils.copyProperties(imMessageBodyEntity,imMessageBody);
        DoStoreGroupMessageDto dto = new DoStoreGroupMessageDto();
        dto.setMessageBody(imMessageBody);
        dto.setGroupChatMessageContent(messageContent);
        rabbitTemplate.convertAndSend(RabbitConstants.STORE_GROUP_MESSAGE,
                "",
                JSONObject.toJSONString(dto));
        messageContent.setMessageKey(imMessageBody.getMessageKey());
    }

    /**
     * GroupChatMessageContent 转 groupMessageHistory
     * @param messageContent
     * @param messageBody
     * @return
     */
    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent
                                                                             messageContent , ImMessageBodyEntity messageBody){
        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBody.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        return result;
    }

    /**
     * 缓存客户端消息防重，格式： appId + :cacheMessage: + messageId
     * @param appId
     * @param messageId
     * @param messageContent
     */
    public void setMessageFromMessageIdCache(Integer appId,String messageId,Object messageContent){
        //appid : cache : messageId
        String key =appId + ":" + RedisConstants.CACHE_MESSAGE + ":" + messageId;
        stringRedisTemplate.opsForValue().set(key,JSONObject.toJSONString(messageContent),300, TimeUnit.SECONDS);
    }

    /**
     * 获取redis中的缓存客户端消息
     * @param appId
     * @param messageId
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> T getMessageFromMessageIdCache(Integer appId,
                                              String messageId,Class<T> clazz){
        //appid : cache : messageId
        String key = appId + ":" + RedisConstants.CACHE_MESSAGE + ":" + messageId;
        String msg = stringRedisTemplate.opsForValue().get(key);
        if(StringUtils.isBlank(msg)){
            return null;
        }
        return JSONObject.parseObject(msg, clazz);
    }

    /**
     * 存储单人离线的消息
     * @param offlineMessage
     */
    public void storeOfflineMessage(OfflineMessageContent offlineMessage){
        // 找到fromId的队列 插入数据 根据messageKey作为分值
        String fromKey = offlineMessage.getAppId() + ":" + RedisConstants.OFFLINE_MESSAGE + ":" + offlineMessage.getFromId();
        // 找到toId的队列 插入数据 根据messageKey作为分值
        String toKey = offlineMessage.getAppId() + ":" + RedisConstants.OFFLINE_MESSAGE + ":" + offlineMessage.getToId();
        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        // 判断队列中的数量是否超过设定值
        if(operations.zCard(fromKey)>appConfig.getOfflineMessageCount()){
            operations.removeRange(fromKey,0,0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(),offlineMessage.getFromId(),offlineMessage.getToId()
        ));
        // 插入 数据 根据messageKey 作为分值
        operations.add(fromKey,JSONObject.toJSONString(offlineMessage),
                offlineMessage.getMessageKey());

        //判断 队列中的数据是否超过设定值
        if(operations.zCard(toKey) > appConfig.getOfflineMessageCount()){
            operations.removeRange(toKey,0,0);
        }

        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(),offlineMessage.getToId(),offlineMessage.getFromId()
        ));
        // 插入 数据 根据messageKey 作为分值
        operations.add(toKey,JSONObject.toJSONString(offlineMessage),
                offlineMessage.getMessageKey());

    }

    /**
     * 存储群离线消息
     * @param offlineMessage
     * @param memberIds
     */
    public void storeGroupOfflineMessage(OfflineMessageContent offlineMessage
            ,List<String> memberIds){

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        //判断 队列中的数据是否超过设定值
        offlineMessage.setConversationType(ConversationTypeEnum.GROUP.getCode());

        for (String memberId : memberIds) {
            // 找到toId的队列
            String toKey = offlineMessage.getAppId() + ":" +
                    RedisConstants.OFFLINE_MESSAGE + ":" +
                    memberId;
            offlineMessage.setConversationId(conversationService.convertConversationId(
                    ConversationTypeEnum.GROUP.getCode(),memberId,offlineMessage.getToId()
            ));
            if(operations.zCard(toKey) > appConfig.getOfflineMessageCount()){
                operations.removeRange(toKey,0,0);
            }
            // 插入 数据 根据messageKey 作为分值
            operations.add(toKey,JSONObject.toJSONString(offlineMessage),
                    offlineMessage.getMessageKey());
        }


    }
}
