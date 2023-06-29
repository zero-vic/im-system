package com.hy.im.common.route.algorithm.random;

import com.hy.im.common.enums.UserErrorCode;
import com.hy.im.common.exception.ApplicationException;
import com.hy.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @ClassName RandomHandle
 * description: 随机算法
 * yao create 2023年06月29日
 * version: 1.0
 */
public class RandomHandle implements RouteHandle {

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if(size==0){
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        int i = ThreadLocalRandom.current().nextInt(size);
        return values.get(i);
    }
}
