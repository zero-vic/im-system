package com.hy.im.codec.pack.friendship;

import lombok.Data;


/**
 * @ClassName UpdateFriendPack
 * description: 修改好友通知报文
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class UpdateFriendPack {

    public String fromId;

    private String toId;

    private String remark;

    private Long sequence;
}
