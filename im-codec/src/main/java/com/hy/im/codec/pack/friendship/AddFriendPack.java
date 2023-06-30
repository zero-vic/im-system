package com.hy.im.codec.pack.friendship;

import lombok.Data;


/**
 * @ClassName AddFriendPack
 * description: 添加好友通知报文
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class AddFriendPack {
    private String fromId;

    /**
     * 备注
     */
    private String remark;
    private String toId;
    /**
     * 好友来源
     */
    private String addSource;
    /**
     * 添加好友时的描述信息（用于打招呼）
     */
    private String addWording;

    private Long sequence;
}
