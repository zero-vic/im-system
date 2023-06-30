package com.hy.im.codec.pack.friendship;

import lombok.Data;

import java.util.List;


/**
 * @ClassName DeleteFriendGroupMemberPack
 * description: 删除好友分组成员通知报文
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class DeleteFriendGroupMemberPack {

    public String fromId;

    private String groupName;

    private List<String> toIds;

    /** 序列号*/
    private Long sequence;
}
