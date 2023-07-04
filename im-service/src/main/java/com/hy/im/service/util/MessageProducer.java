package com.hy.im.service.util;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hy.im.codec.proto.MessagePack;
import com.hy.im.common.constant.RabbitConstants;
import com.hy.im.common.enums.command.Command;
import com.hy.im.common.model.ClientInfo;
import com.hy.im.common.model.UserSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName MessageProducer
 * description: 发送消息的工具类
 * yao create 2023年06月30日
 * version: 1.0
 */
@Component
public class MessageProducer {
    private static Logger log = LoggerFactory.getLogger(MessageProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserSessionUtils userSessionUtils;

    /**
     * 发送消息
     * @param session
     * @param msg
     * @return
     */
    public boolean sendMessage(UserSession session,Object msg){
        try{
            log.info("send message == {}",msg);
            rabbitTemplate.convertAndSend(RabbitConstants.MESSAGE_SERVICE_2_IM,session.getBrokerId()+"",msg);
            return true;
        }catch (Exception e){
            log.error("send msg error : {}",e.getMessage());
            return false;
        }
    }

    /**
     * 封装成数据包发送消息
     * @param toId 接受者id
     * @param command 指令
     * @param msg 消息
     * @param session session
     * @return
     */
    public boolean sendPack(String toId, Command command,Object msg,UserSession session){
        MessagePack<Object> pack = new MessagePack<>();
        pack.setCommand(command.getCommand());
        pack.setAppId(session.getAppId());
        pack.setToId(toId);
        pack.setClientType(session.getClientType());
        pack.setImei(session.getImei());
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(msg));
        pack.setData(jsonObject);
        String body = JSONObject.toJSONString(pack);
        return sendMessage(session, body);
    }

    /**
     * 发送给所有端
     * @param toId
     * @param command
     * @param data
     * @param appId
     */
//    public void sendToUser(String toId,Command command,Object data,Integer appId){
//        List<UserSession> userSessions = userSessionUtils.getUserSession(appId, toId);
//        if(CollUtil.isNotEmpty(userSessions)){
//            userSessions.forEach(session -> sendPack(toId,command,data,session));
//        }
//    }
    public List<ClientInfo> sendToUser(String toId,Command command,Object data,Integer appId){
        List<UserSession> userSession
                = userSessionUtils.getUserSession(appId, toId);
        List<ClientInfo> list = new ArrayList<>();
        for (UserSession session : userSession) {
            boolean b = sendPack(toId, command, data, session);
            if(b){
                list.add(new ClientInfo(session.getAppId(),session.getClientType(),session.getImei()));
            }
        }
        return list;
    }
    /**
     * 发送给所有端
     * @param toId
     * @param clientType
     * @param imei
     * @param command
     * @param data
     * @param appId
     */
    public void sendToUser(String toId, Integer clientType,String imei, Command command,
                           Object data, Integer appId){
        if(clientType != null && StringUtils.isNotBlank(imei)){
            ClientInfo clientInfo = new ClientInfo(appId, clientType, imei);
            sendToUserExceptClient(toId,command,data,clientInfo);
        }else{
            sendToUser(toId,command,data,appId);
        }
    }


    /**
     * 发送给指定的某一端
     * @param toId
     * @param command
     * @param data
     * @param clientInfo
     */
    public void sendToUser(String toId,Command command,Object data,ClientInfo clientInfo){
        UserSession userSession = userSessionUtils.getUserSession(clientInfo.getAppId(), toId, clientInfo.getClientType(), clientInfo.getImei());
        if(userSession!=null){
            sendPack(toId,command,data,userSession);
        }
    }

    /**
     * 发送给出来某一端的其他端
     * @param toId
     * @param command
     * @param data
     * @param clientInfo
     */
    public void sendToUserExceptClient(String toId,Command command,Object data,ClientInfo clientInfo){
        List<UserSession> userSessions = userSessionUtils.getUserSession(clientInfo.getAppId(), toId);
        if(CollUtil.isNotEmpty(userSessions)){
            userSessions.forEach(session -> {
                if(!isMatch(session,clientInfo)){
                    sendPack(toId,command,data,session);
                }
            });
        }
    }

    private boolean isMatch(UserSession sessionDto, ClientInfo clientInfo) {
        return Objects.equals(sessionDto.getAppId(), clientInfo.getAppId())
                && Objects.equals(sessionDto.getImei(), clientInfo.getImei())
                && Objects.equals(sessionDto.getClientType(), clientInfo.getClientType());
    }


}
