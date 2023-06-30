package com.hy.im.service.util;

import com.alibaba.fastjson.JSONObject;
import com.hy.im.common.enums.command.Command;
import com.hy.im.common.model.ClientInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName GroupMessageProducer
 * description: 群组 消息发送工具类
 * yao create 2023年06月30日
 * version: 1.0
 */
@Component
public class GroupMessageProducer {

    @Autowired
    private MessageProducer messageProducer;

    /**
     * todo 待完善
     * 群组 消息发送
     * @param userId
     * @param command
     * @param data
     * @param clientInfo
     */
    public void producer(String userId, Command command, Object data, ClientInfo clientInfo){
        JSONObject json = (JSONObject) JSONObject.toJSON(data);
        String groupId = json.getString("groupId");

    }

}
