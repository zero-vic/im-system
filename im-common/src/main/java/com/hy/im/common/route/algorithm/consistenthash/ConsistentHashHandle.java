package com.hy.im.common.route.algorithm.consistenthash;

import com.hy.im.common.route.RouteHandle;

import java.util.List;

/**
 * @ClassName ConsistentHashHandle
 * description: 一致性hash 算法处理
 * yao create 2023年06月29日
 * version: 1.0
 */
public class ConsistentHashHandle implements RouteHandle {

    private AbstractConsistentHash hash;

    public void setHash(AbstractConsistentHash hash) {
        this.hash = hash;
    }

    @Override
    public String routeServer(List<String> values, String key) {
        return hash.process(values,key);
    }
}
