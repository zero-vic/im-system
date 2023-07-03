package com.hy.im.service.util;

import com.hy.im.common.constant.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class WriteUserSeq {

    //redis
    //uid friend 10
    //    group 12
    //    conversation 123
    @Autowired
    RedisTemplate redisTemplate;

    public void writeUserSeq(Integer appId,String userId,String type,Long seq){
        String key = appId + ":" + RedisConstants.SEQ_PREFIX + ":" + userId;
        redisTemplate.opsForHash().put(key,type,seq);
    }

}