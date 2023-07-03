package com.hy.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.hy.im.common.config.AppConfig;
import com.hy.im.common.constant.RedisConstants;
import com.hy.im.common.enums.BaseErrorCode;
import com.hy.im.common.enums.GateWayErrorCode;
import com.hy.im.common.exception.ApplicationExceptionEnum;
import com.hy.im.common.utils.SigAPI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName IdentityCheck
 * description: 签名校验
 * yao create 2023年07月03日
 * version: 1.0
 */
@Component
public class IdentityCheck {
    private final static Logger log = LoggerFactory.getLogger(IdentityCheck.class);


    // todo 使用单独的表存放每个appid 的 privatekey
    @Autowired
    private AppConfig appConfig;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public ApplicationExceptionEnum checkUserSign(String identity,String appId,String userSign){
        String key =appId +":" +RedisConstants.USER_SIGN+":"+identity+userSign;
        String cashUserSign = stringRedisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(cashUserSign) && Long.valueOf(cashUserSign)>System.currentTimeMillis() / 1000){
            //            this.setIsAdmin(identifier,Integer.valueOf(appId));
            return BaseErrorCode.SUCCESS;
        }

        // 获取密钥
        String privateKey = appConfig.getPrivateKey();

        //根据appid + 秘钥创建sigApi
        SigAPI sigAPI = new SigAPI(Long.valueOf(appId), privateKey);

        //调用sigApi对userSig解密
        JSONObject jsonObject = SigAPI.decodeUserSig(userSign);
        //取出解密后的appid 和 操作人 和 过期时间做匹配，不通过则提示错误

        Long expireTime = 0L;
        Long expireSec = 0L;
        Long time = 0L;
        String decoerAppId = "";
        String decoderidentifier = "";

        try {
            decoerAppId = jsonObject.getString("TLS.appId");
            decoderidentifier = jsonObject.getString("TLS.identifier");
            String expireStr = jsonObject.get("TLS.expire").toString();
            String expireTimeStr = jsonObject.get("TLS.expireTime").toString();
            time = Long.valueOf(expireTimeStr);
            expireSec = Long.valueOf(expireStr);
            expireTime = Long.parseLong(expireTimeStr) + expireSec;
        }catch (Exception e){
            e.printStackTrace();
            log.error("checkUserSig-error:{}",e.getMessage());
        }

        if(!decoderidentifier.equals(identity)){
            return GateWayErrorCode.USERSIGN_OPERATE_NOT_MATE;
        }

        if(!decoerAppId.equals(appId)){
            return GateWayErrorCode.USERSIGN_IS_ERROR;
        }

        if(expireSec == 0L){
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        if(expireTime < System.currentTimeMillis() / 1000){
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        //appid + "xxx" + userId + sign
        String genSig = sigAPI.genUserSig(identity, expireSec,time,null);
        if (genSig.toLowerCase().equals(userSign.toLowerCase()))
        {


            Long etime = expireTime - System.currentTimeMillis() / 1000;
            stringRedisTemplate.opsForValue().set(
                    key,expireTime.toString(),etime, TimeUnit.SECONDS
            );
//            this.setIsAdmin(identifier,Integer.valueOf(appId));
            return BaseErrorCode.SUCCESS;
        }

        return GateWayErrorCode.USERSIGN_IS_ERROR;


    }

}

