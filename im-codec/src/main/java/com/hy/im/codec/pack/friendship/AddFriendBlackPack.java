package com.hy.im.codec.pack.friendship;

import lombok.Data;

/**
 * @ClassName AddFriendBlackPack
 * description: 用户添加黑名单以后tcp通知数据包
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class AddFriendBlackPack {
    private String fromId;

    private String toId;

    private Long sequence;
}
