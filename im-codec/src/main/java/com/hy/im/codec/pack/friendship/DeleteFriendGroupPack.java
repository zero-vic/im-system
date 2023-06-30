package com.hy.im.codec.pack.friendship;

import lombok.Data;

/**
 * @ClassName DeleteFriendGroupPack
 * description: 删除好友分组通知报文
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class DeleteFriendGroupPack {
    public String fromId;

    private String groupName;

    /** 序列号*/
    private Long sequence;
}
