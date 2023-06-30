package com.hy.im.codec.pack.conversation;

import lombok.Data;

/**
 * @ClassName UpdateConversationPack
 * description:
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class UpdateConversationPack {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private Integer conversationType;

    private Long sequence;

}
