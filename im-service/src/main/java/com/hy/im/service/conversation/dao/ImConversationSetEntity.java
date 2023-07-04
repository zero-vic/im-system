package com.hy.im.service.conversation.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @ClassName ImConversationSetEntity
 * description: 会话
 * yao create 2023年07月03日
 * version: 1.0
 */
@Data
@TableName("im_conversation_set")
public class ImConversationSetEntity {

    //会话id 0_fromId_toId
    private String conversationId;

    //会话类型
    private Integer conversationType;

    private String fromId;

    private String toId;

    private int isMute;

    private int isTop;

    private Long sequence;

    private Long readedSequence;

    private Integer appId;
}
