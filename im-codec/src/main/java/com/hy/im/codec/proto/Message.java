package com.hy.im.codec.proto;

import lombok.Data;

/**
 * @ClassName Message
 * description:
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class Message {
    private MessageHeader messageHeader;

    private Object messagePack;

    @Override
    public String toString() {
        return "Message{" +
                "messageHeader=" + messageHeader +
                ", messagePack=" + messagePack +
                '}';
    }
}
