package com.hy.im.codec.pack.friendship;

import lombok.Data;


/**
 * @ClassName DeleteBlackPack
 * description: 删除黑名单通知报文
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class DeleteBlackPack {

    private String fromId;

    private String toId;

    private Long sequence;
}
