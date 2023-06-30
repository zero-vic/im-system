package com.hy.im.service.config;

import com.hy.im.common.config.AppConfig;
import com.hy.im.common.enums.ImUrlRouteWayEnum;
import com.hy.im.common.enums.RouteHashMethodEnum;
import com.hy.im.common.route.RouteHandle;
import com.hy.im.common.route.algorithm.consistenthash.AbstractConsistentHash;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * @ClassName BeanConfig
 * description: bean 配置类
 * yao create 2023年06月30日
 * version: 1.0
 */
@Configuration
public class BeanConfig {
    @Autowired
    AppConfig appConfig;

    /**
     * zk客户端 配置
     * @return
     */
    @Bean
    public ZkClient zkClient(){
        return new ZkClient(appConfig.getZkAddr(),appConfig.getZkConnectTimeOut());
    }

    /**
     * 路由策略 负载均衡策略配置
     * @return
     * @throws Exception
     */
    @Bean
    public RouteHandle routeHandle() throws Exception {
        // 路由策略 负责均衡策略 1随机 2轮训 3hash
        Integer imRouteWay = appConfig.getImRouteWay();
        String routeWay = "";
        ImUrlRouteWayEnum handler = ImUrlRouteWayEnum.getHandler(imRouteWay);
        routeWay = handler.getClazz();
        RouteHandle routeHandle = (RouteHandle) Class.forName(routeWay).newInstance();
        if (handler == ImUrlRouteWayEnum.HASH){
            //如何 是hash ，指定实现一致性hash的算法
            Method setHash = Class.forName(routeWay).getMethod("setHash", AbstractConsistentHash.class);
            // 一致性hash实现的算法
            Integer consistentHashWay = appConfig.getConsistentHashWay();
            String hashway = "";
            RouteHashMethodEnum hashHandle = RouteHashMethodEnum.getHandler(consistentHashWay);
            hashway = hashHandle.getClazz();
            AbstractConsistentHash hash = (AbstractConsistentHash) Class.forName(hashway).newInstance();
            setHash.invoke(routeHandle,hash);

        }
        return routeHandle;
    }
}
