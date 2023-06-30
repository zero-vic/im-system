package com.hy.im.codec.pack.message;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName RecallMessageNotifyPack
 * description: 撤回消息通知报文
 * yao create 2023年06月28日
 * version: 1.0
 */
@Data
@NoArgsConstructor
public class RecallMessageNotifyPack {

    private String fromId;

    private String toId;

    private Long messageKey;

    private Long messageSequence;
}
