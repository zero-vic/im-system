package com.hy.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hy.im.codec.pack.conversation.DeleteConversationPack;
import com.hy.im.codec.pack.conversation.UpdateConversationPack;
import com.hy.im.common.config.AppConfig;
import com.hy.im.common.constant.SeqConstants;
import com.hy.im.common.enums.ConversationErrorCode;
import com.hy.im.common.enums.ConversationTypeEnum;
import com.hy.im.common.enums.command.ConversationEventCommand;
import com.hy.im.common.model.ClientInfo;
import com.hy.im.common.model.SyncReq;
import com.hy.im.common.model.SyncResp;
import com.hy.im.common.model.message.MessageReadedContent;
import com.hy.im.common.response.ResponseVO;
import com.hy.im.service.conversation.dao.ImConversationSetEntity;
import com.hy.im.service.conversation.dao.mapper.ImConversationSetMapper;
import com.hy.im.service.conversation.model.DeleteConversationReq;
import com.hy.im.service.conversation.model.UpdateConversationReq;
import com.hy.im.service.seq.RedisSeq;
import com.hy.im.service.util.MessageProducer;
import com.hy.im.service.util.WriteUserSeq;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Service
public class ConversationService {

    @Autowired
    private ImConversationSetMapper imConversationSetMapper;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private RedisSeq redisSeq;

    @Autowired
    private WriteUserSeq writeUserSeq;

    /**
     * 生成会话id
     * @param type
     * @param fromId
     * @param toId
     * @return
     */
    public String convertConversationId(Integer type,String fromId,String toId){
        return type + "_" + fromId + "_" + toId;
    }

    /**
     * 消息标记已读
     * @param messageReadedContent
     */
    public void  messageMarkRead(MessageReadedContent messageReadedContent){

        String toId = messageReadedContent.getToId();
        // 区分群消息
        if(messageReadedContent.getConversationType() == ConversationTypeEnum.GROUP.getCode()){
            toId = messageReadedContent.getGroupId();
        }
        String conversationId = convertConversationId(messageReadedContent.getConversationType(),
                messageReadedContent.getFromId(), toId);
        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
        query.eq("conversation_id",conversationId);
        query.eq("app_id",messageReadedContent.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(query);
        if(imConversationSetEntity == null){
            imConversationSetEntity = new ImConversationSetEntity();
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + SeqConstants.CONVERSATION);
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent,imConversationSetEntity);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetEntity.setToId(toId);
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.insert(imConversationSetEntity);
            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(),
                    messageReadedContent.getFromId(),SeqConstants.CONVERSATION,seq);
        }else{
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + SeqConstants.CONVERSATION);
            imConversationSetEntity.setSequence(seq);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetMapper.readMark(imConversationSetEntity);
            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(),
                    messageReadedContent.getFromId(),SeqConstants.CONVERSATION,seq);
        }
    }

    /**
     * 删除会话
     * @param req
     * @return
     */
    public ResponseVO deleteConversation(DeleteConversationReq req){

        //置顶 有免打扰
//        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("conversation_id",req.getConversationId());
//        queryWrapper.eq("app_id",req.getAppId());
//        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
//        if(imConversationSetEntity != null){
//            imConversationSetEntity.setIsMute(0);
//            imConversationSetEntity.setIsTop(0);
//            imConversationSetMapper.update(imConversationSetEntity,queryWrapper);
//        }

        if(appConfig.getDeleteConversationSyncMode() == 1){
            DeleteConversationPack pack = new DeleteConversationPack();
            pack.setConversationId(req.getConversationId());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_DELETE,
                    pack,new ClientInfo(req.getAppId(),req.getClientType(),
                            req.getImei()));
        }
        return ResponseVO.successResponse();
    }

    /**
     * 更新会话 置顶or免打扰
     * @param req
     * @return
     */
    public ResponseVO updateConversation(UpdateConversationReq req){

        if(req.getIsTop() == null && req.getIsMute() == null){
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_UPDATE_PARAM_ERROR);
        }
        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id",req.getConversationId());
        queryWrapper.eq("app_id",req.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if(imConversationSetEntity != null){
            long seq = redisSeq.doGetSeq(req.getAppId() + ":" + SeqConstants.CONVERSATION);

            if(req.getIsMute() != null){
                imConversationSetEntity.setIsTop(req.getIsTop());
            }
            if(req.getIsMute() != null){
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.update(imConversationSetEntity,queryWrapper);
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(),
                    SeqConstants.CONVERSATION, seq);

            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(req.getConversationId());
            pack.setIsMute(imConversationSetEntity.getIsMute());
            pack.setIsTop(imConversationSetEntity.getIsTop());
            pack.setSequence(seq);
            pack.setConversationType(imConversationSetEntity.getConversationType());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_UPDATE,
                    pack,new ClientInfo(req.getAppId(),req.getClientType(),
                            req.getImei()));
        }
        return ResponseVO.successResponse();
    }

    public ResponseVO syncConversationSet(SyncReq req) {
        if(req.getMaxLimit() > 100){
            req.setMaxLimit(100);
        }

        SyncResp<ImConversationSetEntity> resp = new SyncResp<>();
        //seq > req.getseq limit maxLimit
        QueryWrapper<ImConversationSetEntity> queryWrapper =
                new QueryWrapper<>();
        queryWrapper.eq("from_id",req.getOperater());
        queryWrapper.gt("sequence",req.getLastSequence());
        queryWrapper.eq("app_id",req.getAppId());
        queryWrapper.last(" limit " + req.getMaxLimit());
        queryWrapper.orderByAsc("sequence");
        List<ImConversationSetEntity> list = imConversationSetMapper
                .selectList(queryWrapper);

        if(!CollectionUtils.isEmpty(list)){
            ImConversationSetEntity maxSeqEntity = list.get(list.size() - 1);
            resp.setDataList(list);
            //设置最大seq
            Long friendShipMaxSeq = imConversationSetMapper.geConversationSetMaxSeq(req.getAppId(), req.getOperater());
            resp.setMaxSequence(friendShipMaxSeq);
            //设置是否拉取完毕
            resp.setCompleted(maxSeqEntity.getSequence() >= friendShipMaxSeq);
            return ResponseVO.successResponse(resp);
        }

        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);

    }
}
