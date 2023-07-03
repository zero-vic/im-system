package com.hy.im.tcp.reciver;

import com.alibaba.fastjson.JSONObject;
import com.hy.im.codec.proto.MessagePack;
import com.hy.im.common.constant.RabbitConstants;
import com.hy.im.tcp.reciver.process.BaseProcess;
import com.hy.im.tcp.reciver.process.ProcessFactory;
import com.hy.im.tcp.utils.MqFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @ClassName MessageReceiver
 * description: 消息接收类
 * yao create 2023年06月29日
 * version: 1.0
 */
public class MessageReceiver {
    private final static Logger log = LoggerFactory.getLogger(MessageReceiver.class);
    private static String brokerId;

    private static void startReceiverMessage(){
        try{
            Channel channel = MqFactory.getChannel(RabbitConstants.MESSAGE_SERVICE_2_IM + brokerId);
            channel.queueDeclare(RabbitConstants.MESSAGE_SERVICE_2_IM+brokerId,true,false,false,null);
            channel.queueBind(RabbitConstants.MESSAGE_SERVICE_2_IM+brokerId,RabbitConstants.IM_2_MESSAGE_SERVICE,brokerId);
            channel.basicConsume(RabbitConstants.MESSAGE_SERVICE_2_IM+brokerId,false,new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //  处理消息服务发来的消息
                    try {
                        String msg = new String(body);
                        log.info(msg);
                        MessagePack messagePack = JSONObject.parseObject(msg, MessagePack.class);
                        BaseProcess massageProcess = ProcessFactory.getMassageProcess(messagePack.getCommand());
                        massageProcess.process(messagePack);
                        channel.basicAck(envelope.getDeliveryTag(),false);
                    } catch (IOException e) {
                        e.printStackTrace();
                        log.error("消息处理异常：{}",e.getMessage());
                        channel.basicNack(envelope.getDeliveryTag(),false,false);
                    }

                }
            });



        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void init() {
        startReceiverMessage();
    }

    public static void init(String brokerId) {
        if (StringUtils.isBlank(MessageReceiver.brokerId)) {
            MessageReceiver.brokerId = brokerId;
        }
        startReceiverMessage();
    }

}
