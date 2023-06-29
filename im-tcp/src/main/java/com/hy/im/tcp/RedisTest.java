package com.hy.im.tcp;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

/**
 * @ClassName RedisTest
 * description: redission 测试类
 * yao create 2023年06月28日
 * version: 1.0
 */
public class RedisTest {
    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        StringCodec stringCodec = new StringCodec();
        config.setCodec(stringCodec);
        RedissonClient redissonClient = Redisson.create(config);
        RBucket<Object> im = redissonClient.getBucket("im");
        System.out.println(im.get());
        im.set("im2");
        System.out.println(im.get());
    }
}
