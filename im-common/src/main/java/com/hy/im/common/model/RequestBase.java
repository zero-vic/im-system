package com.hy.im.common.model;

import lombok.Data;

/**
 * @ClassName RequestBase
 * description:
 * yao create 2023年06月30日
 * version: 1.0
 */
@Data
public class RequestBase {

    private Integer appId;

    private String operater;

    private Integer clientType;

    private String imei;
}
