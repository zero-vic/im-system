package com.hy.im.service.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @ClassName ImMessageHistory
 * description:
 * yao create 2023年07月03日
 * version: 1.0
 */
@Data
@TableName("im_message_history")
public class ImMessageHistoryEntity {

    private Integer appId;

    private String fromId;

    private String toId;

    private String ownerId;

    /** messageBodyId*/
    private Long messageKey;
    /** 序列号*/
    private Long sequence;
    /**
     * 随机数
     */
    private String messageRandom;
    /**
     * 客户端发送的时间
     */
    private Long messageTime;
    /**
     * 服务端插入的时间
     */
    private Long createTime;
}
