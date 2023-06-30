package com.hy.im.codec.pack.friendship;

import lombok.Data;

/**
 * @ClassName ApproverFriendRequestPack
 * description: 审批好友申请通知报文
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
public class ApproverFriendRequestPack {

    private Long id;

    //1同意 2拒绝
    private Integer status;

    private Long sequence;
}
