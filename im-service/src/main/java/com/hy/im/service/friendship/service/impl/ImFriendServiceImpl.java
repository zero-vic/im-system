package com.hy.im.service.friendship.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hy.im.codec.pack.friendship.*;
import com.hy.im.common.config.AppConfig;
import com.hy.im.common.constant.CallbackCommand;
import com.hy.im.common.constant.SeqConstants;
import com.hy.im.common.enums.AllowFriendTypeEnum;
import com.hy.im.common.enums.CheckFriendShipTypeEnum;
import com.hy.im.common.enums.FriendShipErrorCode;
import com.hy.im.common.enums.FriendShipStatusEnum;
import com.hy.im.common.enums.command.FriendshipEventCommand;
import com.hy.im.common.exception.ApplicationException;
import com.hy.im.common.model.RequestBase;
import com.hy.im.common.model.SyncReq;
import com.hy.im.common.model.SyncResp;
import com.hy.im.common.response.ResponseVO;
import com.hy.im.service.friendship.dao.ImFriendShipEntity;
import com.hy.im.service.friendship.dao.mapper.ImFriendShipMapper;
import com.hy.im.service.friendship.model.callback.AddFriendAfterCallbackDto;
import com.hy.im.service.friendship.model.callback.AddFriendBlackAfterCallbackDto;
import com.hy.im.service.friendship.model.callback.DeleteFriendAfterCallbackDto;
import com.hy.im.service.friendship.model.req.*;
import com.hy.im.service.friendship.model.resp.CheckFriendShipResp;
import com.hy.im.service.friendship.model.resp.ImportFriendShipResp;
import com.hy.im.service.friendship.service.ImFriendService;
import com.hy.im.service.friendship.service.ImFriendShipRequestService;
import com.hy.im.service.seq.RedisSeq;
import com.hy.im.service.user.dao.ImUserDataEntity;
import com.hy.im.service.user.service.ImUserService;
import com.hy.im.service.util.CallbackService;
import com.hy.im.service.util.MessageProducer;
import com.hy.im.service.util.WriteUserSeq;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Service
public class ImFriendServiceImpl implements ImFriendService {

    @Autowired
    ImFriendShipMapper imFriendShipMapper;

    @Autowired
    ImUserService imUserService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImFriendService imFriendService;

    @Autowired
    ImFriendShipRequestService imFriendShipRequestService;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;

    @Override
    public ResponseVO importFriendShip(ImporFriendShipReq req) {

        if(req.getFriendItem().size() > 100){
            return ResponseVO.errorResponse(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }
        ImportFriendShipResp resp = new ImportFriendShipResp();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();

        for (ImporFriendShipReq.ImportFriendDto dto:
             req.getFriendItem()) {
            ImFriendShipEntity entity = new ImFriendShipEntity();
            BeanUtils.copyProperties(dto,entity);
            entity.setAppId(req.getAppId());
            entity.setFromId(req.getFromId());
            try {
                int insert = imFriendShipMapper.insert(entity);
                if(insert == 1){
                    successId.add(dto.getToId());
                }else{
                    errorId.add(dto.getToId());
                }
            }catch (Exception e){
                e.printStackTrace();
                errorId.add(dto.getToId());
            }

        }

        resp.setErrorId(errorId);
        resp.setSuccessId(successId);

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addFriend(AddFriendReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if(!fromInfo.isOk()){
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if(!toInfo.isOk()){
            return toInfo;
        }

        if(appConfig.isAddFriendBeforeCallback()){
            ResponseVO callbackResp = callbackService
                    .beforeCallback(req.getAppId(),
                            CallbackCommand.ADD_FRIEND_BEFORE
                            ,JSONObject.toJSONString(req));
            if(!callbackResp.isOk()){
                return callbackResp;
            }
        }

        ImUserDataEntity data = toInfo.getData();

        if(data.getFriendAllowType() != null && data.getFriendAllowType() == AllowFriendTypeEnum.NOT_NEED.getCode()){
            return this.doAddFriend(req,req.getFromId(), req.getToItem(), req.getAppId());
        }else{
            QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
            query.eq("app_id",req.getAppId());
            query.eq("from_id",req.getFromId());
            query.eq("to_id",req.getToItem().getToId());
            ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
            if(fromItem == null || fromItem.getStatus()
                    != FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()){
                //插入一条好友申请的数据
                ResponseVO responseVO = imFriendShipRequestService.addFienshipRequest(req.getFromId(), req.getToItem(), req.getAppId());
                if(!responseVO.isOk()){
                    return responseVO;
                }
            }else{
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            }

        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO updateFriend(UpdateFriendReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if(!fromInfo.isOk()){
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if(!toInfo.isOk()){
            return toInfo;
        }

        ResponseVO responseVO = this.doUpdate(req.getFromId(), req.getToItem(), req.getAppId());
        if(responseVO.isOk()){
            UpdateFriendPack updateFriendPack = new UpdateFriendPack();
            updateFriendPack.setRemark(req.getToItem().getRemark());
            updateFriendPack.setToId(req.getToItem().getToId());
            messageProducer.sendToUser(req.getFromId(),
                    req.getClientType(),req.getImei(), FriendshipEventCommand
            .FRIEND_UPDATE,updateFriendPack,req.getAppId());

            if (appConfig.isModifyFriendAfterCallback()) {
                AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToItem(req.getToItem());
                callbackService.beforeCallback(req.getAppId(),
                        CallbackCommand.UPDATE_FRIEND_AFTER, JSONObject
                                .toJSONString(callbackDto));
            }
        }
        return responseVO;
    }

    @Transactional
    public ResponseVO doUpdate(String fromId, FriendDto dto, Integer appId){


        long seq = redisSeq.doGetSeq(appId + ":" + SeqConstants.FRIENDSHIP);
        UpdateWrapper<ImFriendShipEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(ImFriendShipEntity::getAddSource,dto.getAddSource())
                .set(ImFriendShipEntity::getExtra,dto.getExtra())
                .set(ImFriendShipEntity::getFriendSequence,seq)
                .set(ImFriendShipEntity::getRemark,dto.getRemark())
                .eq(ImFriendShipEntity::getAppId,appId)
                .eq(ImFriendShipEntity::getToId,dto.getToId())
                .eq(ImFriendShipEntity::getFromId,fromId);

        int update = imFriendShipMapper.update(null, updateWrapper);
        if(update == 1){
            //之后回调
//            if (appConfig.isModifyFriendAfterCallback()){
//                AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
//                callbackDto.setFromId(fromId);
//                callbackDto.setToItem(dto);
//                callbackService.beforeCallback(appId,
//                        Constants.CallbackCommand.UpdateFriendAfter, JSONObject
//                                .toJSONString(callbackDto));
//            }
            writeUserSeq.writeUserSeq(appId,fromId,SeqConstants.FRIENDSHIP,seq);
            return ResponseVO.successResponse();
        }

        return ResponseVO.errorResponse();
    }

    @Transactional
    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId){

        //A-B
        //Friend表插入A 和 B 两条记录
        //查询是否有记录存在，如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id",appId);
        query.eq("from_id",fromId);
        query.eq("to_id",dto.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        long seq = 0L;
        if(fromItem == null){
            //走添加逻辑。
            fromItem = new ImFriendShipEntity();
            seq = redisSeq.doGetSeq(appId+":"+SeqConstants.FRIENDSHIP);
            fromItem.setAppId(appId);
            fromItem.setFriendSequence(seq);
            fromItem.setFromId(fromId);
//            entity.setToId(to);
            BeanUtils.copyProperties(dto,fromItem);
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            if(insert != 1){
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
            writeUserSeq.writeUserSeq(appId,fromId,SeqConstants.FRIENDSHIP,seq);
        } else{
            //如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态

            if(fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()){
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            } else{
                ImFriendShipEntity update = new ImFriendShipEntity();

                if(StringUtils.isNotBlank(dto.getAddSource())){
                    update.setAddSource(dto.getAddSource());
                }

                if(StringUtils.isNotBlank(dto.getRemark())){
                    update.setRemark(dto.getRemark());
                }

                if(StringUtils.isNotBlank(dto.getExtra())){
                    update.setExtra(dto.getExtra());
                }
                seq = redisSeq.doGetSeq(appId+":"+SeqConstants.FRIENDSHIP);
                update.setFriendSequence(seq);
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

                int result = imFriendShipMapper.update(update, query);
                if(result != 1){
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }
                writeUserSeq.writeUserSeq(appId,fromId,SeqConstants.FRIENDSHIP,seq);
            }

        }

        QueryWrapper<ImFriendShipEntity> toQuery = new QueryWrapper<>();
        toQuery.eq("app_id",appId);
        toQuery.eq("from_id",dto.getToId());
        toQuery.eq("to_id",fromId);
        ImFriendShipEntity toItem = imFriendShipMapper.selectOne(toQuery);
        if(toItem == null){
            toItem = new ImFriendShipEntity();
            toItem.setAppId(appId);
            toItem.setFromId(dto.getToId());
            BeanUtils.copyProperties(dto,toItem);
            toItem.setToId(fromId);
            toItem.setFriendSequence(seq);
            toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            toItem.setCreateTime(System.currentTimeMillis());
//            toItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
            int insert = imFriendShipMapper.insert(toItem);
            writeUserSeq.writeUserSeq(appId,dto.getToId(),SeqConstants.FRIENDSHIP,seq);
        }else{
            if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() !=
            toItem.getStatus()){
                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setFriendSequence(seq);
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                imFriendShipMapper.update(update,toQuery);
                writeUserSeq.writeUserSeq(appId,dto.getToId(),SeqConstants.FRIENDSHIP,seq);
            }
        }

        //发送给from
        AddFriendPack addFriendPack = new AddFriendPack();
        BeanUtils.copyProperties(fromItem,addFriendPack);
        addFriendPack.setSequence(seq);
        if(requestBase != null){
            messageProducer.sendToUser(fromId,requestBase.getClientType(),
                   requestBase.getImei(), FriendshipEventCommand.FRIEND_ADD,addFriendPack
            ,requestBase.getAppId());
        }else {
            messageProducer.sendToUser(fromId,
                    FriendshipEventCommand.FRIEND_ADD,addFriendPack
                    ,requestBase.getAppId());
        }

        AddFriendPack addFriendToPack = new AddFriendPack();
        BeanUtils.copyProperties(toItem,addFriendPack);
        messageProducer.sendToUser(toItem.getFromId(),
                FriendshipEventCommand.FRIEND_ADD,addFriendToPack
                ,requestBase.getAppId());

        //之后回调
        if (appConfig.isAddFriendAfterCallback()){
            AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
            callbackDto.setFromId(fromId);
            callbackDto.setToItem(dto);
            callbackService.beforeCallback(appId,
                    CallbackCommand.ADD_FRIEND_AFTER, JSONObject
                            .toJSONString(callbackDto));
        }

        return ResponseVO.successResponse();
    }


    @Override
    public ResponseVO deleteFriend(DeleteFriendReq req) {

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id",req.getAppId());
        query.eq("from_id",req.getFromId());
        query.eq("to_id",req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        if(fromItem == null){
            return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        }else{
            if(fromItem.getStatus() != null && fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()){
                ImFriendShipEntity update = new ImFriendShipEntity();
                long seq = redisSeq.doGetSeq(req.getAppId() + ":" + SeqConstants.FRIENDSHIP);
                update.setFriendSequence(seq);
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
                imFriendShipMapper.update(update,query);
                writeUserSeq.writeUserSeq(req.getAppId(),req.getFromId(),SeqConstants.FRIENDSHIP,seq);
                DeleteFriendPack deleteFriendPack = new DeleteFriendPack();
                deleteFriendPack.setFromId(req.getFromId());
                deleteFriendPack.setSequence(seq);
                deleteFriendPack.setToId(req.getToId());
                messageProducer.sendToUser(req.getFromId(),
                        req.getClientType(), req.getImei(),
                        FriendshipEventCommand.FRIEND_DELETE,
                        deleteFriendPack, req.getAppId());

                //之后回调
                if (appConfig.isAddFriendAfterCallback()){
                    DeleteFriendAfterCallbackDto callbackDto = new DeleteFriendAfterCallbackDto();
                    callbackDto.setFromId(req.getFromId());
                    callbackDto.setToId(req.getToId());
                    callbackService.beforeCallback(req.getAppId(),
                            CallbackCommand.DELETE_FRIEND_AFTER, JSONObject
                                    .toJSONString(callbackDto));
                }

            }else{
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }
        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteAllFriend(DeleteFriendReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id",req.getAppId());
        query.eq("from_id",req.getFromId());
        query.eq("status",FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
        imFriendShipMapper.update(update,query);

        DeleteAllFriendPack deleteFriendPack = new DeleteAllFriendPack();
        deleteFriendPack.setFromId(req.getFromId());
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(), FriendshipEventCommand.FRIEND_ALL_DELETE,
                deleteFriendPack, req.getAppId());

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getAllFriendShip(GetAllFriendShipReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id",req.getAppId());
        query.eq("from_id",req.getFromId());
        return ResponseVO.successResponse(imFriendShipMapper.selectList(query));
    }

    @Override
    public ResponseVO getRelation(GetRelationReq req) {

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id",req.getAppId());
        query.eq("from_id",req.getFromId());
        query.eq("to_id",req.getToId());

        ImFriendShipEntity entity = imFriendShipMapper.selectOne(query);
        if(entity == null){
            return ResponseVO.errorResponse(FriendShipErrorCode.REPEATSHIP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(entity);
    }

    @Override
    public ResponseVO checkBlck(CheckFriendShipReq req) {

        Map<String, Integer> toIdMap
                = req.getToIds().stream().collect(Collectors
                .toMap(Function.identity(), s -> 0));
        List<CheckFriendShipResp> result = new ArrayList<>();
        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            result = imFriendShipMapper.checkFriendShipBlack(req);
        } else {
            result = imFriendShipMapper.checkFriendShipBlackBoth(req);
        }

        Map<String, Integer> collect = result.stream()
                .collect(Collectors
                        .toMap(CheckFriendShipResp::getToId,
                                CheckFriendShipResp::getStatus));
        for (String toId:
                toIdMap.keySet()) {
            if(!collect.containsKey(toId)){
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setStatus(toIdMap.get(toId));
                result.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(result);
    }

    @Override
    public ResponseVO syncFriendshipList(SyncReq req) {
        // 一次拉取最多100
        if(req.getMaxLimit() > 100){
            req.setMaxLimit(100);
        }

        SyncResp<ImFriendShipEntity> resp = new SyncResp<>();
        //seq > req.getseq limit maxLimit
        QueryWrapper<ImFriendShipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_id",req.getOperater());
        queryWrapper.gt("friend_sequence",req.getLastSequence());
        queryWrapper.eq("app_id",req.getAppId());
        queryWrapper.last(" limit " + req.getMaxLimit());
        queryWrapper.orderByAsc("friend_sequence");
        List<ImFriendShipEntity> list = imFriendShipMapper.selectList(queryWrapper);

        if(!CollectionUtils.isEmpty(list)){
            ImFriendShipEntity maxSeqEntity = list.get(list.size() - 1);
            resp.setDataList(list);
            //设置最大seq
            Long friendShipMaxSeq = imFriendShipMapper.getFriendShipMaxSeq(req.getAppId(), req.getOperater());
            resp.setMaxSequence(friendShipMaxSeq);
            //设置是否拉取完毕
            resp.setCompleted(maxSeqEntity.getFriendSequence() >= friendShipMaxSeq);
            return ResponseVO.successResponse(resp);
        }

        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);
    }

    @Override
    public List<String> getAllFriendId(String userId, Integer appId) {
        return imFriendShipMapper.getAllFriendId(userId,appId);
    }

    @Override
    public ResponseVO addBlack(AddFriendShipBlackReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if(!fromInfo.isOk()){
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToId(), req.getAppId());
        if(!toInfo.isOk()){
            return toInfo;
        }
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id",req.getAppId());
        query.eq("from_id",req.getFromId());
        query.eq("to_id",req.getToId());

        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        Long seq = 0L;
        if(fromItem == null){
            //走添加逻辑。
            seq = redisSeq.doGetSeq(req.getAppId() + ":" + SeqConstants.FRIENDSHIP);

            fromItem = new ImFriendShipEntity();
            fromItem.setFromId(req.getFromId());
            fromItem.setToId(req.getToId());
            fromItem.setFriendSequence(seq);
            fromItem.setAppId(req.getAppId());
            fromItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            if(insert != 1){
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
            writeUserSeq.writeUserSeq(req.getAppId(),req.getFromId(),SeqConstants.FRIENDSHIP,seq);

        } else{
            //如果存在则判断状态，如果是拉黑，则提示已拉黑，如果是未拉黑，则修改状态
            if(fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode()){
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
            }

            else {
                seq = redisSeq.doGetSeq(req.getAppId() + ":" + SeqConstants.FRIENDSHIP);

                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setFriendSequence(seq);
                update.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
                int result = imFriendShipMapper.update(update, query);
                if(result != 1){
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_BLACK_ERROR);
                }
                writeUserSeq.writeUserSeq(req.getAppId(),req.getFromId(),SeqConstants.FRIENDSHIP,seq);

            }
        }

        AddFriendBlackPack addFriendBlackPack = new AddFriendBlackPack();
        addFriendBlackPack.setFromId(req.getFromId());
        addFriendBlackPack.setSequence(seq);
        addFriendBlackPack.setToId(req.getToId());
        //发送tcp通知
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_BLACK_ADD, addFriendBlackPack, req.getAppId());

        //之后回调
        if (appConfig.isAddFriendShipBlackAfterCallback()){
            AddFriendBlackAfterCallbackDto callbackDto = new AddFriendBlackAfterCallbackDto();
            callbackDto.setFromId(req.getFromId());
            callbackDto.setToId(req.getToId());
            callbackService.beforeCallback(req.getAppId(),
                    CallbackCommand.ADD_BLACK_AFTER, JSONObject
                            .toJSONString(callbackDto));
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteBlack(DeleteBlackReq req) {
        QueryWrapper queryFrom = new QueryWrapper<>()
                .eq("from_id", req.getFromId())
                .eq("app_id", req.getAppId())
                .eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryFrom);
        if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_IS_NOT_YOUR_BLACK);
        }

        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + SeqConstants.FRIENDSHIP);

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setFriendSequence(seq);
        update.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
        int update1 = imFriendShipMapper.update(update, queryFrom);
        if(update1 == 1){
            writeUserSeq.writeUserSeq(req.getAppId(),req.getFromId(),SeqConstants.FRIENDSHIP,seq);
            DeleteBlackPack deleteFriendPack = new DeleteBlackPack();
            deleteFriendPack.setFromId(req.getFromId());
            deleteFriendPack.setSequence(seq);
            deleteFriendPack.setToId(req.getToId());
            messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(), FriendshipEventCommand.FRIEND_BLACK_DELETE,
                    deleteFriendPack, req.getAppId());

            //之后回调
            if (appConfig.isAddFriendShipBlackAfterCallback()){
                AddFriendBlackAfterCallbackDto callbackDto = new AddFriendBlackAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToId(req.getToId());
                callbackService.beforeCallback(req.getAppId(),
                        CallbackCommand.DELETE_BLACK, JSONObject
                                .toJSONString(callbackDto));
            }
        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO checkFriendship(CheckFriendShipReq req) {

        Map<String, Integer> result
                = req.getToIds().stream()
                .collect(Collectors.toMap(Function.identity(), s -> 0));

        List<CheckFriendShipResp> resp = new ArrayList<>();

        if(req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()){
            resp =imFriendShipMapper.checkFriendShip(req);
        }else {
            resp =imFriendShipMapper.checkFriendShipBoth(req);
        }

        Map<String, Integer> collect = resp.stream()
                .collect(Collectors.toMap(CheckFriendShipResp::getToId
                        , CheckFriendShipResp::getStatus));

        for (String toId : result.keySet()){
            if(!collect.containsKey(toId)){
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setStatus(result.get(toId));
                resp.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(resp);
    }




}
