package com.hy.im.service.util;

import com.alibaba.fastjson.JSONObject;
import com.hy.im.common.constant.RedisConstants;
import com.hy.im.common.enums.ImConnectStatusEnum;
import com.hy.im.common.model.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UserSessionUtils
 * description: usersession 工具类
 * yao create 2023年06月30日
 * version: 1.0
 */
@Component
public class UserSessionUtils {

    @Autowired
    StringRedisTemplate stringRedisTemplate;


    /**
     * 获取用户所有的session
     * @param appId
     * @param userId
     * @return
     */
    public List<UserSession> getUserSession(Integer appId, String userId){

        String userSessionKey = appId + RedisConstants.USER_SESSION_CONSTANTS + userId;
        Map<Object, Object> entries =
                stringRedisTemplate.opsForHash().entries(userSessionKey);
        List<UserSession> list = new ArrayList<>();
        Collection<Object> values = entries.values();
        for (Object o : values){
            String str = (String) o;
            UserSession session =
                    JSONObject.parseObject(str, UserSession.class);
            if(session.getConnectState().equals(ImConnectStatusEnum.ONLINE_STATUS.getCode())){
                list.add(session);
            }
        }
        return list;
    }



    /**
     * 获取 指定端的session
     * @param appId
     * @param userId
     * @param clientType
     * @param imei
     * @return
     */
    public UserSession getUserSession(Integer appId,String userId,Integer clientType,String imei){

        String userSessionKey = appId + RedisConstants.USER_SESSION_CONSTANTS + userId;
        String hashKey = clientType + ":" + imei;
        Object o = stringRedisTemplate.opsForHash().get(userSessionKey, hashKey);
        UserSession session =
                JSONObject.parseObject(o.toString(), UserSession.class);
        return session;
    }
}
