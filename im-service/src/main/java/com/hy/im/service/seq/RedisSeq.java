package com.hy.im.service.seq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @ClassName RedisSeq
 * description:
 * yao create 2023年07月03日
 * version: 1.0
 */
public class RedisSeq {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public long doGetSeq(String key){
        return stringRedisTemplate.opsForValue().increment(key);
    }
}
