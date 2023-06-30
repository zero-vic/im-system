package com.hy.im.tcp.register;

import com.hy.im.codec.config.BootstrapConfig;
import com.hy.im.common.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName RegistryZK
 * description: zookeeper 注册类
 * yao create 2023年06月29日
 * version: 1.0
 */
public class RegistryZK implements Runnable{
    private final static Logger log = LoggerFactory.getLogger(RegistryZK.class);

    private ZKit zKit;

    private String ip;

    private BootstrapConfig.TcpConfig tcpConfig;

    public RegistryZK(ZKit zKit,String ip,BootstrapConfig.TcpConfig tcpConfig){
        this.zKit = zKit;
        this.ip = ip;
        this.tcpConfig = tcpConfig;
    }

    @Override
    public void run() {
        zKit.createRootNode();
        String tcpPath = Constants.IM_CORE_ZKROOT+Constants.IM_CORE_ZKROOT_TCP+"/"+ip+":"+tcpConfig.getTcpPort();
        zKit.createNode(tcpPath);
        log.info("Registry zookeeper tcpPath success, msg=[{}]", tcpPath);

        String webPath = Constants.IM_CORE_ZKROOT+Constants.IM_CORE_ZKROOT_WEB+"/"+ip+":"+tcpConfig.getWebSocketPort();
        zKit.createNode(webPath);
        log.info("Registry zookeeper webPath success, msg=[{}]",webPath);
    }
}
