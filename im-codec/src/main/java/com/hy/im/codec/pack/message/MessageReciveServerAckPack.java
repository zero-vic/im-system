package com.hy.im.codec.pack.message;

import lombok.Data;

/**
 * @ClassName MessageReciveServerAckPack
 * description: 服务端发起的ack包
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class MessageReciveServerAckPack {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;
    /**
     * 是否是服务端发起
     */
    private Boolean serverSend;
}
