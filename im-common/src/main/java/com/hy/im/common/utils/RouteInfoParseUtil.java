package com.hy.im.common.utils;


import com.hy.im.common.enums.BaseErrorCode;
import com.hy.im.common.exception.ApplicationException;
import com.hy.im.common.route.RouteInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @since JDK 1.8
 */
public class RouteInfoParseUtil {
    private final static Logger log = LoggerFactory.getLogger(RouteInfoParseUtil.class);
    public static RouteInfo parse(String info){
        try {
            String[] serverInfo = info.split(":");
            RouteInfo routeInfo =  new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1])) ;
            return routeInfo ;
        }catch (Exception e){
            log.error("路由参数处理错误：{}",e.getMessage());
            throw new ApplicationException(BaseErrorCode.PARAMETER_ERROR) ;
        }
    }
}
