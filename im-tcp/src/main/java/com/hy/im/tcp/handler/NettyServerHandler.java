package com.hy.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hy.im.codec.pack.LoginPack;
import com.hy.im.codec.proto.Message;
import com.hy.im.common.constant.Constants;
import com.hy.im.common.constant.RedisConstants;
import com.hy.im.common.enums.ImConnectStatusEnum;
import com.hy.im.common.enums.command.SystemCommand;
import com.hy.im.common.model.UserClientDto;
import com.hy.im.common.model.UserSession;
import com.hy.im.tcp.redis.RedisManager;
import com.hy.im.tcp.utils.SessionSocketHolder;
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

    public NettyServerHandler(Integer brokerId){
        this.brokerId = brokerId;
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
        }

        System.out.println("msg:"+msg.toString());
    }
}
