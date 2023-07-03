package com.hy.im.service.friendship.service;


import com.hy.im.common.response.ResponseVO;
import com.hy.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.hy.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.hy.im.service.friendship.model.req.DeleteFriendShipGroupReq;

/**
 * @author: Chackylee
 * @description:
 **/
public interface ImFriendShipGroupService {

    public ResponseVO addGroup(AddFriendShipGroupReq req);

    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req);

    public ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId);

    public Long updateSeq(String fromId, String groupName, Integer appId);
}
