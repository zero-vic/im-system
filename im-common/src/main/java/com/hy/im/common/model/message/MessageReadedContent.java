package com.hy.im.common.model.message;

import com.hy.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @ClassName MessageReadedContent
 * description: 消息已读内容
 * yao create 2023年07月04日
 * version: 1.0
 */
@Data
public class MessageReadedContent extends ClientInfo {

    private long messageSequence;

    private String fromId;

    private String groupId;

    private String toId;
    /**
     * 会话类型
     */
    private Integer conversationType;

}
