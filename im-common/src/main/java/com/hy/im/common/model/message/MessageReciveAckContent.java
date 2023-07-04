package com.hy.im.common.model.message;


import com.hy.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @ClassName MessageReciveAckContent
 * description: 消息确认ack
 * yao create 2023年07月03日
 * version: 1.0
 */
@Data
public class MessageReciveAckContent extends ClientInfo {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;


}
