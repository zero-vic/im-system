package com.hy.im.message.model;


import com.hy.im.common.model.message.GroupChatMessageContent;
import com.hy.im.message.dao.ImMessageBodyEntity;
import lombok.Data;


@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
