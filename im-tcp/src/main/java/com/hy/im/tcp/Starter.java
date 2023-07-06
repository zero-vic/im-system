package com.hy.im.tcp;

import com.hy.im.codec.config.BootstrapConfig;
import com.hy.im.tcp.reciver.MessageReceiver;
import com.hy.im.tcp.redis.RedisManager;
import com.hy.im.tcp.register.RegistryZK;
import com.hy.im.tcp.register.ZKit;
import com.hy.im.tcp.server.ImServer;
import com.hy.im.tcp.server.ImWebSocketServer;
import com.hy.im.tcp.utils.MqFactory;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
            // 初始化rabbitmq
            MqFactory.init(bootstrapConfig.getIm().getRabbitmq());
            MessageReceiver.init(bootstrapConfig.getIm().getBrokerId().toString());
            // 初始化zookeeper
            registerZk(bootstrapConfig);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(500);
        }
    }

    public static void registerZk(BootstrapConfig config) throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        ZkClient zkClient = new ZkClient(config.getIm().getZkConfig().getZkAddr(),
                config.getIm().getZkConfig().getZkConnectTimeOut());
        ZKit zKit = new ZKit(zkClient);
        RegistryZK registryZK = new RegistryZK(zKit, hostAddress, config.getIm());
        Thread thread = new Thread(registryZK);
        thread.start();
    }
}
