package com.hy.im.service.message.model.req;


import com.hy.im.common.model.RequestBase;
import lombok.Data;

/**
 * @ClassName SendMessageReq
 * description: 管理员发送消息的参数
 * yao create 2023年07月04日
 * version: 1.0
 */
@Data
public class SendMessageReq extends RequestBase {

    //客户端传的messageId
    private String messageId;

    private String fromId;

    private String toId;

    private int messageRandom;

    private long messageTime;

    private String messageBody;
    /**
     * 这个字段缺省或者为 0 表示需要计数，为 1 表示本条消息不需要计数，即右上角图标数字不增加
     */
    private int badgeMode;

    private Long messageLifeTime;

    private Integer appId;

}
