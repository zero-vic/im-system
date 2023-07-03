package com.hy.im.service.message.service;

import com.hy.im.common.config.AppConfig;
import com.hy.im.common.enums.*;
import com.hy.im.common.response.ResponseVO;
import com.hy.im.service.friendship.dao.ImFriendShipEntity;
import com.hy.im.service.friendship.model.req.GetRelationReq;
import com.hy.im.service.friendship.service.ImFriendService;
import com.hy.im.service.group.dao.ImGroupEntity;
import com.hy.im.service.group.model.resp.GetRoleInGroupResp;
import com.hy.im.service.group.service.ImGroupMemberService;
import com.hy.im.service.group.service.ImGroupService;
import com.hy.im.service.user.dao.ImUserDataEntity;
import com.hy.im.service.user.service.ImUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName CheckSendMessageService
 * description: 发送消息检验服务
 * yao create 2023年07月03日
 * version: 1.0
 */
@Service
public class CheckSendMessageService {
    private final  static Logger log = LoggerFactory.getLogger(CheckSendMessageService.class);
    @Autowired
    ImUserService imUserService;

    @Autowired
    ImFriendService imFriendService;

    @Autowired
    ImGroupService imGroupService;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    AppConfig appConfig;

    /**
     * 检测发送方 是否存在,是否被禁言，是否被禁用
     * @param fromId
     * @param appId
     * @return
     */
    public ResponseVO checkSenderForvidAndMute(String fromId,Integer appId){
        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(fromId, appId);
        if(!singleUserInfo.isOk()){
            return singleUserInfo;
        }
        ImUserDataEntity user = singleUserInfo.getData();
        if(user.getForbiddenFlag().equals(UserForbiddenFlagEnum.FORBIBBEN.getCode())){
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_FORBIBBEN);
        } else if (user.getSilentFlag().equals(UserSilentFlagEnum.MUTE.getCode())) {
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_MUTE);
        }

        return ResponseVO.successResponse();

    }

    /**
     * 检查好友关系
     * @param fromId
     * @param toId
     * @param appId
     * @return
     */
    public ResponseVO checkFriendShip(String fromId,String toId,Integer appId){
        // 发送消息是否校验关系链
        if(appConfig.isSendMessageCheckFriend()){
            GetRelationReq fromReq = new GetRelationReq();
            fromReq.setToId(toId);
            fromReq.setAppId(appId);
            fromReq.setFromId(fromId);
            ResponseVO<ImFriendShipEntity> fromRelation = imFriendService.getRelation(fromReq);
            if(!fromRelation.isOk()){
                return fromRelation;
            }
            fromReq.setFromId(toId);
            fromReq.setToId(fromId);
            fromReq.setAppId(appId);
            ResponseVO<ImFriendShipEntity> toRelation = imFriendService.getRelation(fromReq);
            if(!toRelation.isOk()){
                return toRelation;
            }
            if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != fromRelation.getData().getStatus()){
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != toRelation.getData().getStatus()){
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }
            // 校验黑名单
            if(appConfig.isSendMessageCheckBlack()){
                if(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()
                        != fromRelation.getData().getBlack()){
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
                }

                if(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()
                        != toRelation.getData().getBlack()){
                    return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_BLACK_YOU);
                }
            }


        }
        return ResponseVO.successResponse();
    }

    public ResponseVO checkGroupMessage(String fromId,String groupId,Integer appId){
        ResponseVO responseVO = checkSenderForvidAndMute(fromId, appId);
        if(!responseVO.isOk()){
            return responseVO;
        }
        // 判断群逻辑
        ResponseVO<ImGroupEntity> group = imGroupService.getGroup(groupId, appId);
        if(!group.isOk()){
            return group;
        }
        //判断群成员是否在群内
        ResponseVO<GetRoleInGroupResp> roleInGroupOne = imGroupMemberService.getRoleInGroupOne(groupId, fromId, appId);
        if(!roleInGroupOne.isOk()){
            return roleInGroupOne;
        }
        GetRoleInGroupResp data = roleInGroupOne.getData();

        //判断群是否被禁言
        //如果禁言 只有裙管理和群主可以发言
        ImGroupEntity groupData = group.getData();
        if(groupData.getMute() == GroupMuteTypeEnum.MUTE.getCode()
                && (data.getRole() == GroupMemberRoleEnum.MAMAGER.getCode() ||
                data.getRole() == GroupMemberRoleEnum.OWNER.getCode())){
            return ResponseVO.errorResponse(GroupErrorCode.THIS_GROUP_IS_MUTE);
        }

        if(data.getSpeakDate() != null && data.getSpeakDate() > System.currentTimeMillis()){
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MEMBER_IS_SPEAK);
        }

        return ResponseVO.successResponse();

    }
}
