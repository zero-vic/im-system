package com.hy.im.service.util;

import com.hy.im.common.config.AppConfig;
import com.hy.im.common.response.ResponseVO;
import com.hy.im.common.utils.HttpRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName CallbackService
 * description: 回调服务
 * yao create 2023年06月30日
 * version: 1.0
 */
@Component
public class CallbackService {
    private final static Logger log = LoggerFactory.getLogger(CallbackService.class);

    @Autowired
    private HttpRequestUtils httpRequestUtils;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ShareThreadPool shareThreadPool;

    /**
     * 回调
     * @param appId
     * @param callbackCommand
     * @param jsonBody
     */
    public void callback(Integer appId,String callbackCommand,String jsonBody){

        shareThreadPool.submit(() ->{
            try{
                // todo callbackurl 后续设计一张表存储 目前使用配置文件的方式实现
                httpRequestUtils.doPost(appConfig.getCallbackUrl(),Object.class,builderUrlParams(appId,callbackCommand),jsonBody,null);
            }catch (Exception e){
                log.error("callback 回调{} : {}出现异常 ： {} ",callbackCommand , appId, e.getMessage());
            }
        });

    }

    /**
     * 之前回调
     * @param appId
     * @param callbackCommand
     * @param jsonBody
     * @return
     */
    public ResponseVO beforeCallback(Integer appId, String callbackCommand, String jsonBody){
        try {
            ResponseVO responseVO = httpRequestUtils.doPost("", ResponseVO.class, builderUrlParams(appId, callbackCommand),
                    jsonBody, null);
            return responseVO;
        }catch (Exception e){
            log.error("callback 之前 回调{} : {}出现异常 ： {} ",callbackCommand , appId, e.getMessage());
            return ResponseVO.successResponse();
        }
    }



    public Map builderUrlParams(Integer appId, String command) {
        Map<String,Object> map = new HashMap<>();
        map.put("appId", appId);
        map.put("command", command);
        return map;
    }
}
