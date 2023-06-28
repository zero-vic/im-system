package com.hy.im.tcp.redis;

import com.hy.im.codec.config.BootstrapConfig;
import org.redisson.api.RedissonClient;

/**
 * @ClassName RedisManager
 * description:redis 管理类
 * yao create 2023年06月28日
 * version: 1.0
 */
public class RedisManager {
    private static RedissonClient redissonClient;

    private static Integer loginModel;

    public static void init(BootstrapConfig config){
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getIm().getRedis());
    }

    public static RedissonClient getRedissonClient() {
        return redissonClient;
    }
}
