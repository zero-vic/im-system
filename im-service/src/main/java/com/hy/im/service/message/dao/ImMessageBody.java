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
public class ImMessageBody {

    private Integer appId;

    /** messageBodyId*/
    private Long messageKey;

    /** messageBody*/
    private String messageBody;

    private String securityKey;

    private Long messageTime;

    private Long createTime;

    private String extra;

    private Integer delFlag;
}
