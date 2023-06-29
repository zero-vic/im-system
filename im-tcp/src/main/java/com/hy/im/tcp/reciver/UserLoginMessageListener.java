package com.hy.im.tcp.reciver;

import com.alibaba.fastjson.JSON;
import com.hy.im.codec.proto.MessagePack;
import com.hy.im.common.constant.Constants;
import com.hy.im.common.constant.RedisConstants;
import com.hy.im.common.enums.ClientTypeEnum;
import com.hy.im.common.enums.DeviceMultiLoginEnum;
import com.hy.im.common.enums.command.SystemCommand;
import com.hy.im.common.model.UserClientDto;
import com.hy.im.tcp.redis.RedisManager;
import com.hy.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ClassName UserLoginMessageListener
 * description:
 *  * 多端同步：1单端登录：一端在线：踢掉除了本clinetType + imel 的设备
 *  *         2双端登录：允许pc/mobile 其中一端登录 + web端 踢掉除了本clinetType + imel 以外的web端设备
 *  *         3 三端登录：允许手机+pc+web，踢掉同端的其他imei 除了web
 *  *         4 不做任何处理
 * yao create 2023年06月29日
 * version: 1.0
 */
public class UserLoginMessageListener {
    private final static Logger log = LoggerFactory.getLogger(UserLoginMessageListener.class);

    private Integer loginModel;

    public UserLoginMessageListener(Integer loginModel){
        this.loginModel = loginModel;
    }

    public void listenerUserLogin(){
        // redis 消息订阅模式 获取登陆消息
        RTopic topic = RedisManager.getRedissonClient().getTopic(RedisConstants.USER_LOGIN_CHANNEL);
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String msg) {
                log.info("收到用户上线通知：{}",msg);
                UserClientDto dto = JSON.parseObject(msg, UserClientDto.class);
                List<NioSocketChannel> channels = SessionSocketHolder.get(dto.getAppId(), dto.getUserId());
                for (NioSocketChannel channel : channels) {
                    Integer clientType = (Integer) channel.attr(AttributeKey.valueOf(Constants.CLIENT_TYPE)).get();
                    String imei = (String) channel.attr(AttributeKey.valueOf(Constants.IMEI)).get();
                    if(loginModel == DeviceMultiLoginEnum.ONE.getLoginMode()){
                       // 单端登录：一端在线：踢掉除了本clinetType + imel 的设备
                        // 说明其他端的再登陆
                        if(!(clientType+":"+imei).equals(dto.getClientType()+":"+dto.getImei())){
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) channel.attr(AttributeKey.valueOf(Constants.USER_ID)).get());
                            pack.setUserId((String) channel.attr(AttributeKey.valueOf(Constants.USER_ID)).get());
                            pack.setCommand(SystemCommand.MUTUAL_LOGIN.getCommand());
                            channel.writeAndFlush(pack);
                        }
                    } else if (loginModel == DeviceMultiLoginEnum.TWO.getLoginMode()) {
                        // 双端登录：允许pc/mobile 其中一端登录 + web端 踢掉除了本clinetType + imel 以外的web端设备
                        if(dto.getClientType() == ClientTypeEnum.WEB.getCode() || clientType == ClientTypeEnum.WEB.getCode()){
                            continue;
                        }
                        if(!(clientType + ":" + imei).equals(dto.getClientType()+":"+dto.getImei())){
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) channel.attr(AttributeKey.valueOf(Constants.USER_ID)).get());
                            pack.setUserId((String) channel.attr(AttributeKey.valueOf(Constants.USER_ID)).get());
                            pack.setCommand(SystemCommand.MUTUAL_LOGIN.getCommand());
                            channel.writeAndFlush(pack);
                        }

                    } else if (loginModel == DeviceMultiLoginEnum.THREE.getLoginMode()) {
                        //三端登录：允许手机+pc+web，踢掉同端的其他imei 除了web
                        if(dto.getClientType() == ClientTypeEnum.WEB.getCode()){
                            continue;
                        }
                        Boolean isSameClient = false;
                        if ((clientType == ClientTypeEnum.ANDROID.getCode() || clientType == ClientTypeEnum.IOS.getCode())
                                && (dto.getClientType() == ClientTypeEnum.ANDROID.getCode() || dto.getClientType() == ClientTypeEnum.IOS.getCode())){
                            isSameClient = true;
                        }
                        if ((clientType == ClientTypeEnum.WINDOWS.getCode() || clientType == ClientTypeEnum.MAC.getCode())
                                && (dto.getClientType() == ClientTypeEnum.WINDOWS.getCode() || dto.getClientType() == ClientTypeEnum.MAC.getCode())){
                            isSameClient = true;
                        }
                        if(!(clientType + ":" + imei).equals(dto.getClientType()+":"+dto.getImei())){
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) channel.attr(AttributeKey.valueOf(Constants.USER_ID)).get());
                            pack.setUserId((String) channel.attr(AttributeKey.valueOf(Constants.USER_ID)).get());
                            pack.setCommand(SystemCommand.MUTUAL_LOGIN.getCommand());
                            channel.writeAndFlush(pack);
                        }
                    }
                }
            }
        });
    }

}
