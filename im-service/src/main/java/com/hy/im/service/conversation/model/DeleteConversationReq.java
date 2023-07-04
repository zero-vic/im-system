package com.hy.im.service.conversation.model;

import com.hy.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;


@Data
public class DeleteConversationReq extends RequestBase {

    @NotBlank(message = "会话id不能为空")
    private String conversationId;

    @NotBlank(message = "fromId不能为空")
    private String fromId;

}
