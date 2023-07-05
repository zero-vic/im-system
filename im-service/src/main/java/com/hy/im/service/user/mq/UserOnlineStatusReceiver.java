package com.hy.im.service.user.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hy.im.common.constant.RabbitConstants;
import com.hy.im.common.enums.command.UserEventCommand;
import com.hy.im.service.user.model.UserStatusChangeNotifyContent;
import com.hy.im.service.user.service.ImUserStatusService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName UserOnlineStatusReceiver
 * description: 用户在线状态监听
 * yao create 2023年07月05日
 * version: 1.0
 */
@Component
public class UserOnlineStatusReceiver {
    private final static Logger log = LoggerFactory.getLogger(UserOnlineStatusReceiver.class);

    @Autowired
    private ImUserStatusService imUserStatusService;


    /**
     * 订阅MQ单聊消息队列--处理
     * @param message
     * @param headers
     * @param channel
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitConstants.IM_2_USER_SERVICE, durable = "true"),
            exchange = @Exchange(value = RabbitConstants.IM_2_USER_SERVICE, durable = "true")
    ), concurrency = "1")
    @RabbitHandler
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String,Object> headers,
                              Channel channel) throws Exception {
        long start = System.currentTimeMillis();
        Thread t = Thread.currentThread();
        String msg = new String(message.getBody(), "utf-8");
        log.info("CHAT MSG FROM QUEUE :::::" + msg);
        //deliveryTag 用于回传 rabbitmq 确认该消息处理成功
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);

        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            if(Objects.equals(command, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand())){
                UserStatusChangeNotifyContent content = JSON.parseObject(msg, new TypeReference<UserStatusChangeNotifyContent>() {
                }.getType());
                // 处理用户的在线状态
                imUserStatusService.processUserOnlineStatusNotify(content);
            }


            channel.basicAck(deliveryTag,false);
        }catch (Exception e){
            log.error("处理消息出现异常：{}",e.getMessage());
            log.error("RMQ_CHAT_TRAN_ERROR", e);
            log.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        }finally {
            long end = System.currentTimeMillis();
            log.debug("channel {} basic-Ack ,it costs {} ms,threadName = {},threadId={}", channel, end - start, t.getName(), t.getId());
        }
    }

}
