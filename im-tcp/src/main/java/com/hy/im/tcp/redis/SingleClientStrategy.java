package com.hy.im.tcp.redis;

import com.hy.im.codec.config.BootstrapConfig;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

/**
 * @ClassName SingleClientStrategy
 * description: redis 单机客户端策略类
 * yao create 2023年06月28日
 * version: 1.0
 */
public class SingleClientStrategy {

    public RedissonClient getRedissonClient(BootstrapConfig.RedisConfig redisConfig){
        Config config = new Config();
        String node = redisConfig.getSingle().getAddress();
        node = node.startsWith("redis://") ? node : "redis://" + node;
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(node)
                .setDatabase(redisConfig.getDatabase())
                .setTimeout(redisConfig.getTimeout())
                .setConnectionMinimumIdleSize(redisConfig.getPoolMinIdle())
                .setConnectionPoolSize(redisConfig.getPoolSize())
                .setConnectTimeout(redisConfig.getTimeout());
        if(StringUtils.isNotBlank(redisConfig.getPassword())){
            singleServerConfig.setPassword(redisConfig.getPassword());
        }
        StringCodec stringCodec = new StringCodec();
        config.setCodec(stringCodec);
        return Redisson.create(config);
    }
}
