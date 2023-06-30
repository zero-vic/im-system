package com.hy.im.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName ClientInfo
 * description: 客户端信息
 * yao create 2023年06月30日
 * version: 1.0
 */
@Data
@NoArgsConstructor
public class ClientInfo {

    private Integer appId;

    private Integer clientType;

    private String imei;

    public ClientInfo(Integer appId, Integer clientType, String imei) {
        this.appId = appId;
        this.clientType = clientType;
        this.imei = imei;
    }
}
