package com.hy.im.common.route;

import java.util.List;

/**
 * 路由负载均衡算法处理接口
 */
public interface RouteHandle {

     String routeServer(List<String> values, String key);

}
