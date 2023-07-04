package com.hy.im.service.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @ClassName ImMessageBody
 * description:
 * yao create 2023年07月03日
 * version: 1.0
 */
@Data
@TableName("im_message_body")
public class ImMessageBodyEntity {
    /**
     * appid
     */
    private Integer appId;

    /**
     * messageBodyId
     */
    private Long messageKey;

    /**
     *  消息体的内容
     */
    private String messageBody;

    private String securityKey;
    /**
     * 客户端发送消息的时间
     */
    private Long messageTime;
    /**
     * 服务端创建消息的时间
     */
    private Long createTime;
    /**
     * 扩展字段
     */
    private String extra;
    /**
     * 删除标识
     */
    private Integer delFlag;
}
