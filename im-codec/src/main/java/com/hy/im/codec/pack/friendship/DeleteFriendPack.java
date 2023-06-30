package com.hy.im.codec.pack.friendship;

import lombok.Data;

/**
 * @ClassName DeleteFriendPack
 * description: 删除好友通知报文
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class DeleteFriendPack {

    private String fromId;

    private String toId;

    private Long sequence;
}
