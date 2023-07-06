package com.hy.im.tcp.publish;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hy.im.codec.proto.Message;
import com.hy.im.codec.proto.MessageHeader;
import com.hy.im.common.constant.RabbitConstants;
import com.hy.im.common.enums.command.CommandType;
import com.hy.im.tcp.utils.MqFactory;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * @ClassName MqMessageProducer
 * description: mq 消息 生产类 (给用户发消息)
 * yao create 2023年06月29日
 * version: 1.0
 */
public class MqMessageProducer {
    private final static Logger log = LoggerFactory.getLogger(MqMessageProducer.class);
    public static void sendMessage(Message message, Integer command){
        Channel channel = null;
        String commandStr = command.toString();
        String commandSub = commandStr.substring(0, 1);
        CommandType commandType = CommandType.getCommandType(commandSub);
        String channelName = "";
        if(commandType == CommandType.MESSAGE){
            channelName = RabbitConstants.IM_2_MESSAGE_SERVICE;
        } else if (commandType == CommandType.GROUP) {
            channelName = RabbitConstants.IM_2_GROUP_SERVICE;
        } else if (commandType == CommandType.FRIEND) {
            channelName = RabbitConstants.IM_2_FRIENDSHIP_SERVICE;
        } else if (commandType == CommandType.USER) {
            channelName = RabbitConstants.IM_2_USER_SERVICE;
        }

        try {
            channel = MqFactory.getChannel(channelName);
            JSONObject json = (JSONObject) JSONObject.toJSON(message.getMessagePack());
            json.put("command",command);
            json.put("clientType",message.getMessageHeader().getClientType());
            json.put("imei",message.getMessageHeader().getImei());
            json.put("appId",message.getMessageHeader().getAppId());
            channel.basicPublish(channelName,"",null,json.toJSONString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("发送消息出现异常：{}",e.getMessage());
        }
    }
    public static void sendMessage(Object message, MessageHeader header, Integer command){
        Channel channel = null;
        String com = command.toString();
        String commandSub = com.substring(0, 1);
        CommandType commandType = CommandType.getCommandType(commandSub);
        String channelName = "";
        if(commandType == CommandType.MESSAGE){
            channelName = RabbitConstants.IM_2_MESSAGE_SERVICE;
        } else if (commandType == CommandType.GROUP) {
            channelName = RabbitConstants.IM_2_GROUP_SERVICE;
        } else if (commandType == CommandType.FRIEND) {
            channelName = RabbitConstants.IM_2_FRIENDSHIP_SERVICE;
        } else if (commandType == CommandType.USER) {
            channelName = RabbitConstants.IM_2_USER_SERVICE;
        }

        try {
            channel = MqFactory.getChannel(channelName);

            JSONObject o = (JSONObject) JSON.toJSON(message);
            o.put("command",command);
            o.put("clientType",header.getClientType());
            o.put("imei",header.getImei());
            o.put("appId",header.getAppId());
            channel.basicPublish(channelName,"",
                    null, o.toJSONString().getBytes());
        }catch (Exception e){
            log.error("发送消息出现异常：{}",e.getMessage());
        }
    }

}
