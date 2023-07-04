package com.hy.im.service.message.model.resp;

import lombok.Data;

/**
 * @ClassName SendMessageResp
 * description: 管理员发送消息的返回参数
 * yao create 2023年07月04日
 * version: 1.0
 */
@Data
public class SendMessageResp {

    private Long messageKey;

    private Long messageTime;

}
