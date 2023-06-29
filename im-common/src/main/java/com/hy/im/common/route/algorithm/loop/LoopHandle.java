package com.hy.im.common.route.algorithm.loop;

import com.hy.im.common.enums.UserErrorCode;
import com.hy.im.common.exception.ApplicationException;
import com.hy.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName LoopHandle
 * description: 轮训算法
 * yao create 2023年06月29日
 * version: 1.0
 */
public class LoopHandle implements RouteHandle {
    private AtomicLong index = new AtomicLong();

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if(size==0){
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        Long l = index.incrementAndGet() % size;
        l= l < 0 ? 0L : l;
        return values.get(l.intValue());
    }
}
