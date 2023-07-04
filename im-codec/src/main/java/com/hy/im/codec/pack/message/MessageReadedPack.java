package com.hy.im.codec.pack.message;

import lombok.Data;

/**
 * @ClassName MessageReadedPack
 * description: 消息已读pack
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class MessageReadedPack {

    private long messageSequence;

    private String fromId;

    private String groupId;

    private String toId;

    private Integer conversationType;
}
