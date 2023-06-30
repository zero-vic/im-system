package com.hy.im.codec.pack.message;

import lombok.Data;

/**
 * @ClassName MessageReciveServerAckPack
 * description:
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class MessageReciveServerAckPack {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;

    private Boolean serverSend;
}
