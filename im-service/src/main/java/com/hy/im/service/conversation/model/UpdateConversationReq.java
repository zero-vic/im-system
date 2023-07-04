package com.hy.im.service.conversation.model;


import com.hy.im.common.model.RequestBase;
import lombok.Data;


@Data
public class UpdateConversationReq extends RequestBase {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private String fromId;


}
