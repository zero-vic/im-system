package com.hy.im.common.model.message;

import lombok.Data;

import java.util.List;

/**
 * @ClassName GroupChatMessageContent
 * description:群聊消息内容
 * yao create 2023年07月03日
 * version: 1.0
 */
@Data
public class GroupChatMessageContent extends MessageContent{
    private String groupId;

    private List<String> memberId;
}
