package com.hy.im.codec.pack.friendship;

import lombok.Data;


/**
 * @ClassName ReadAllFriendRequestPack
 * description: 已读好友申请通知报文
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class ReadAllFriendRequestPack {

    private String fromId;

    private Long sequence;
}
