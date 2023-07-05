package com.hy.im.common.model;

import lombok.Data;

/**
 * 增量同步请求体
 */
@Data
public class SyncReq extends RequestBase {

    //客户端最大seq
    private Long lastSequence;
    //一次拉取多少
    private Integer maxLimit;

}