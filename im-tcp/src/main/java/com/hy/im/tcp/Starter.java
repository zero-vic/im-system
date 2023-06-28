package com.hy.im.tcp;

import com.hy.im.codec.config.BootstrapConfig;
import com.hy.im.tcp.redis.RedisManager;
import com.hy.im.tcp.server.ImServer;
import com.hy.im.tcp.server.ImWebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @ClassName Starter
 * description: 启动类
 * yao create 2023年06月28日
 * version: 1.0
 */
public class Starter {
    private final static Logger log = LoggerFactory.getLogger(Starter.class);


    /**
     *
     *     client IOS 安卓 pc(windows mac) web //支持json 也支持 protobuf
     *     appId
     *     28 + imei + body
     *     请求头（指令 版本 clientType 消息解析类型 imei长度 appId bodylen）+ imei号 + 请求体
     *     len+body
     *
     */
    public static void main(String[] args) {
       if (args.length>0){
           log.info("config.yaml path :{}",args[0]);
           start(args[0]);
       }
    }

    private static void start(String path){
        try {
            // 加载配置文件
            Yaml yaml = new Yaml();
            FileInputStream inputStream = new FileInputStream(path);
            BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);
            // 启动imserver
            new ImServer(bootstrapConfig.getIm()).start();
            new ImWebSocketServer(bootstrapConfig.getIm()).start();
            // 初始化redis
            RedisManager.init(bootstrapConfig);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(500);
        }
    }
}
