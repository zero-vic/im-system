package com.hy.im.message.model;


import com.hy.im.common.model.message.MessageContent;
import com.hy.im.message.dao.ImMessageBodyEntity;
import lombok.Data;


@Data
public class DoStoreP2PMessageDto {

    private MessageContent messageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
