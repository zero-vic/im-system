package com.hy.im.service.friendship.service;


import com.hy.im.common.response.ResponseVO;
import com.hy.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.hy.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;

/**
 * @author: Chackylee
 * @description:
 **/
public interface ImFriendShipGroupMemberService {

    public ResponseVO addGroupMember(AddFriendShipGroupMemberReq req);

    public ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req);

    public int doAddGroupMember(Long groupId, String toId);

    public int clearGroupMember(Long groupId);
}
