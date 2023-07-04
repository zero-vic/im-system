package com.hy.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hy.im.codec.pack.LoginPack;
import com.hy.im.codec.pack.message.ChatMessageAck;
import com.hy.im.codec.proto.Message;
import com.hy.im.codec.proto.MessagePack;
import com.hy.im.common.constant.Constants;
import com.hy.im.common.constant.RedisConstants;
import com.hy.im.common.enums.ImConnectStatusEnum;
import com.hy.im.common.enums.command.GroupEventCommand;
import com.hy.im.common.enums.command.MessageCommand;
import com.hy.im.common.enums.command.SystemCommand;
import com.hy.im.common.model.UserClientDto;
import com.hy.im.common.model.UserSession;
import com.hy.im.common.model.message.CheckSendMessageReq;
import com.hy.im.common.response.ResponseVO;
import com.hy.im.tcp.feign.FeignMessageService;
import com.hy.im.tcp.publish.MqMessageProducer;
import com.hy.im.tcp.redis.RedisManager;
import com.hy.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * @ClassName NettyServerHandler
 * description:
 * yao create 2023年06月28日
 * version: 1.0
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private final static Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

    private Integer brokerId;
    private FeignMessageService feignMessageService;
    public NettyServerHandler(Integer brokerId,String logicUrl){
        this.brokerId = brokerId;
        // 初始化feign
        feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 3500))//设置超时时间
                .target(FeignMessageService.class, logicUrl);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Integer command = msg.getMessageHeader().getCommand();
        // 用户登陆的逻辑
        if(command == SystemCommand.LOGIN.getCommand()){
            LoginPack loginPack = JSON.parseObject(JSON.toJSONString(msg.getMessagePack()), new TypeReference<LoginPack>() {
            }.getType());
            String userId = loginPack.getUserId();
            Integer appId = msg.getMessageHeader().getAppId();
            Integer clientType = msg.getMessageHeader().getClientType();
            String imei = msg.getMessageHeader().getImei();
            String clientImei = clientType+imei;
            //为channel设置属性
            ctx.channel().attr(AttributeKey.valueOf(Constants.USER_ID)).set(userId);
            ctx.channel().attr(AttributeKey.valueOf(Constants.APP_ID)).set(appId);
            ctx.channel().attr(AttributeKey.valueOf(Constants.CLIENT_TYPE)).set(clientType);
            ctx.channel().attr(AttributeKey.valueOf(Constants.IMEI)).set(imei);
            ctx.channel().attr(AttributeKey.valueOf(Constants.CLIENT_IMEI)).set(clientImei);

            //存session
            UserSession userSession = new UserSession();
            userSession.setAppId(msg.getMessageHeader().getAppId());
            userSession.setClientType(msg.getMessageHeader().getClientType());
            userSession.setUserId(loginPack.getUserId());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            userSession.setBrokerId(brokerId);
            userSession.setImei(msg.getMessageHeader().getImei());
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                userSession.setBrokerHost(localHost.getHostAddress());
            }catch (Exception e){
                e.printStackTrace();
            }


            // 存到redis

            RedissonClient redissonClient = RedisManager.getRedissonClient();
            String userSessionKey = appId + RedisConstants.USER_SESSION_CONSTANTS + userId;
            RMap<String, String> map = redissonClient.getMap(userSessionKey);
            map.put(clientType+":" + imei, JSONObject.toJSONString(userSession));
            // 使用redis的发布订阅模式来通知用户上线, 实现多端登陆
            UserClientDto dto = new UserClientDto();
            dto.setAppId(appId);
            dto.setClientType(clientType);
            dto.setUserId(userId);
            dto.setImei(imei);
            RTopic topic = redissonClient.getTopic(RedisConstants.USER_LOGIN_CHANNEL);
            topic.publish(JSON.toJSONString(dto));


            // 存channel
            SessionSocketHolder.put(appId,userId,clientType, (NioSocketChannel) ctx.channel(),imei);

        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            // 登出
            // 删除usersession 删除redis
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
        } else if (command == SystemCommand.PING.getCommand()) {
            // 心跳检测 处理
            // 添加读取时间
            ctx.channel().attr(AttributeKey.valueOf(Constants.READ_TIME)).set(System.currentTimeMillis());
        } else if (command == MessageCommand.MSG_P2P.getCommand() ||
                command == GroupEventCommand.MSG_GROUP.getCommand()){
            try{
                String toId = "";
                CheckSendMessageReq req = new CheckSendMessageReq();
                req.setAppId(msg.getMessageHeader().getAppId());
                req.setCommand(msg.getMessageHeader().getCommand());
                JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()));
                String fromId = jsonObject.getString("fromId");
                if(command == MessageCommand.MSG_P2P.getCommand()){
                    toId = jsonObject.getString("toId");
                }else {
                    toId = jsonObject.getString("groupId");
                }
                req.setToId(toId);
                req.setFromId(fromId);

                ResponseVO responseVO = feignMessageService.checkSendMessage(req);
                if(responseVO.isOk()){
                    MqMessageProducer.sendMessage(msg,command);
                }else {
                    // 返回ack
                    Integer ackCommand = 0;
                    if (command == MessageCommand.MSG_P2P.getCommand()) {
                        ackCommand = MessageCommand.MSG_ACK.getCommand();
                    } else {
                        ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                    }

                    ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                    responseVO.setData(chatMessageAck);
                    MessagePack<ResponseVO> ack = new MessagePack<>();
                    ack.setData(responseVO);
                    ack.setCommand(ackCommand);
                    ctx.channel().writeAndFlush(ack);
                }
            }catch (Exception e){
                e.printStackTrace();
                log.error("tcp 校验聊天信息 出现异常:{}",e.getMessage());
            }
        }
        else {
            // 消息发送给逻辑层
            MqMessageProducer.sendMessage(msg,command);
        }

        System.out.println("msg:"+msg.toString());
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);

    }
}
