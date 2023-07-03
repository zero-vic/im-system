package com.hy.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hy.im.common.constant.RabbitConstants;
import com.hy.im.common.enums.command.MessageCommand;
import com.hy.im.common.model.message.MessageContent;
import com.hy.im.service.message.service.P2PMessageService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * @ClassName ChatOperateReceiver
 * description: 聊天操作消息监听
 * yao create 2023年07月03日
 * version: 1.0
 */
@Component
public class ChatOperateReceiver {
    private final static Logger log = LoggerFactory.getLogger(ChatOperateReceiver.class);

    @Autowired
    private P2PMessageService p2PMessageService;

    /**
     * // 使用 @Payload 和 @Headers 注解可以消息中的 body 与 headers 信息
     */

    @RabbitListener(
            bindings =@QueueBinding(
                value = @Queue(value = RabbitConstants.IM_2_MESSAGE_SERVICE,durable = "true"),
                exchange = @Exchange(value = RabbitConstants.IM_2_MESSAGE_SERVICE,durable = "true")
            ),concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String,Object> headers,
                              Channel channel) throws Exception {

        String msg = new String(message.getBody(),"utf-8");
        log.info("CHAT MSG FORM QUEUE ::: {}", msg);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);

        try {
            JSONObject jsonObject = JSONObject.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            if(command.equals(MessageCommand.MSG_P2P.getCommand())){
                // 单聊消息处理
                MessageContent messageContent = jsonObject.toJavaObject(MessageContent.class);
                p2PMessageService.process(messageContent);

            }

            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            log.error("处理消息出现异常：{}", e.getMessage());
            log.error("RMQ_CHAT_TRAN_ERROR", e);
            log.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        }


    }

}
