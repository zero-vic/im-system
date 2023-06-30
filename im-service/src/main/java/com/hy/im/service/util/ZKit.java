package com.hy.im.service.util;

import com.hy.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName ZKit
 * description: zookeeper 配置类
 * yao create 2023年06月29日
 * version: 1.0
 */
@Component
public class ZKit {
    private static Logger logger = LoggerFactory.getLogger(ZKit.class);

    @Autowired
    private ZkClient zkClient;
    /**
     * get all TCP server node from zookeeper
     *
     * @return
     */
    public List<String> getAllTcpNode() {
        List<String> children = zkClient.getChildren(Constants.IM_CORE_ZKROOT + Constants.IM_CORE_ZKROOT_TCP);
//        logger.info("Query all node =[{}] success.", JSON.toJSONString(children));
        return children;
    }

    /**
     * get all WEB server node from zookeeper
     *
     * @return
     */
    public List<String> getAllWebNode() {
        List<String> children = zkClient.getChildren(Constants.IM_CORE_ZKROOT + Constants.IM_CORE_ZKROOT_WEB);
//        logger.info("Query all node =[{}] success.", JSON.toJSONString(children));
        return children;
    }

}
