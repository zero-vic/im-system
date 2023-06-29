package com.hy.im.common.route;

import lombok.Data;

/**
 * @ClassName RouteInfo
 * description: 0.0
 * yao create 2023年06月29日
 * version: 1.0
 */
@Data
public class RouteInfo {

    private String ip;
    private Integer port;

    public RouteInfo(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }
}
