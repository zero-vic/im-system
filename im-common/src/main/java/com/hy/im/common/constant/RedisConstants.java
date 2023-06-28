package com.hy.im.common.constant;

/**
 * @ClassName RedisConstants
 * description: redis 常量类
 * yao create 2023年06月28日
 * version: 1.0
 */
public class RedisConstants {
    /**
     * userSign，格式：appId:userSign:
     */
    public static final String USER_SIGN = "userSign";

    /**
     * 用户上线通知channel
     */
    public static final String USER_LOGIN_CHANNEL
            = "signal/channel/LOGIN_USER_INNER_QUEUE";


    /**
     * 用户session，appId + UserSessionConstants + 用户id 例如10000：userSession：lld
     */
    public static final String USER_SESSION_CONSTANTS = ":userSession:";

    /**
     * 缓存客户端消息防重，格式： appId + :cacheMessage: + messageId
     */
    public static final String CACHE_MESSAGE = "cacheMessage";

    public static final String OFFLINE_MESSAGE = "offlineMessage";

    /**
     * seq 前缀
     */
    public static final String SEQ_PREFIX = "seq";

    /**
     * 用户订阅列表，格式 ：appId + :subscribe: + userId。Hash结构，filed为订阅自己的人
     */
    public static final String SUB_SCRIBE = "subscribe";

    /**
     * 用户自定义在线状态，格式 ：appId + :userCustomerStatus: + userId。set，value为用户id
     */
    public static final String USER_CUSTOMER_STATUS = "userCustomerStatus";
}
