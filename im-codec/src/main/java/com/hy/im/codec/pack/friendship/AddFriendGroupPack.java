package com.hy.im.codec.pack.friendship;

import lombok.Data;


/**
 * @ClassName AddFriendGroupPack
 * description: 用户创建好友分组通知包
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class AddFriendGroupPack {
    public String fromId;

    private String groupName;

    /** 序列号*/
    private Long sequence;
}
