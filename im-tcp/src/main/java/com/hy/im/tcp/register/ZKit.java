package com.hy.im.tcp.register;

import com.hy.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;

/**
 * @ClassName ZKit
 * description: zookeeper 配置类
 * yao create 2023年06月29日
 * version: 1.0
 */
public class ZKit {

    private ZkClient zkClient;

    public ZKit(ZkClient zkClient){
        this.zkClient = zkClient;
    }

    /**
     * 创建root节点
     */
    public void createRootNode(){
        boolean exists = zkClient.exists(Constants.IM_CORE_ZKROOT);
        if(!exists){
            zkClient.createPersistent(Constants.IM_CORE_ZKROOT);
        }
        boolean existsTcp = zkClient.exists(Constants.IM_CORE_ZKROOT + Constants.IM_CORE_ZKROOT_TCP);
        if(!existsTcp){
            zkClient.createPersistent(Constants.IM_CORE_ZKROOT + Constants.IM_CORE_ZKROOT_TCP);
        }
        boolean existsWeb = zkClient.exists(Constants.IM_CORE_ZKROOT + Constants.IM_CORE_ZKROOT_WEB);
        if(!existsWeb){
            zkClient.createPersistent(Constants.IM_CORE_ZKROOT + Constants.IM_CORE_ZKROOT_WEB);
        }

    }

    /**
     * 创建节点
     *  ip + port
     */
    public void createNode(String path){
        boolean exists = zkClient.exists(path);
        if(!exists){
            zkClient.createPersistent(path);
        }
    }

}
