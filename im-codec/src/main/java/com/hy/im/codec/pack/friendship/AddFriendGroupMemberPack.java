package com.hy.im.codec.pack.friendship;

import lombok.Data;

import java.util.List;


/**
 * @ClassName AddFriendGroupMemberPack
 * description: 好友分组添加成员通知包
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class AddFriendGroupMemberPack {

    public String fromId;

    private String groupName;

    private List<String> toIds;

    /** 序列号*/
    private Long sequence;
}
