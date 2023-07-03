package com.hy.im.common.model.message;

import com.hy.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @ClassName MessageContent
 * description: 消息内容
 * yao create 2023年07月03日
 * version: 1.0
 */
@Data
public class MessageContent extends ClientInfo {
    /**
     * 消息id
     */
    private String messageId;
    /**
     * 发送人id
     */
    private String fromId;
    /**
     * 接受人id
     */
    private String toId;
    /**
     * 消息体
     */
    private String messageBody;
    /**
     * 消息时间
     */
    private Long messageTime;

    private String extra;

    private Long messageKey;

    private long messageSequence;
}
