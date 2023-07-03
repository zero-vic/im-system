package com.hy.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.hy.im.common.enums.BaseErrorCode;
import com.hy.im.common.enums.GateWayErrorCode;
import com.hy.im.common.exception.ApplicationExceptionEnum;
import com.hy.im.common.response.ResponseVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @ClassName GateWayInterceptor
 * description: 鉴权拦截器
 * yao create 2023年07月03日
 * version: 1.0
 */
public class GateWayInterceptor implements HandlerInterceptor {

    @Autowired
    private IdentityCheck identityCheck;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        // 获取 appId
        String appId = request.getParameter("appId");
        if(StringUtils.isBlank(appId)){
            resp(ResponseVO.errorResponse(GateWayErrorCode.APPID_NOT_EXIST),response);
            return false;
        }

        // 获取 identifier
        String identifier = request.getParameter("identifier");
        if(StringUtils.isBlank(identifier)){
            resp(ResponseVO.errorResponse(GateWayErrorCode.OPERATER_NOT_EXIST),response);
            return false;
        }

        // 获取 userSign
        String userSign = request.getParameter("userSign");
        if(StringUtils.isBlank(userSign)){
            resp(ResponseVO.errorResponse(GateWayErrorCode.USERSIGN_NOT_EXIST),response);
            return false;
        }
        //签名和操作人和appid是否匹配
        ApplicationExceptionEnum applicationExceptionEnum = identityCheck.checkUserSign(identifier, appId, userSign);
        if(applicationExceptionEnum != BaseErrorCode.SUCCESS){
            resp(ResponseVO.errorResponse(applicationExceptionEnum),response);
            return false;
        }

        return true;

    }

    /**
     * 结果返回
     * @param responseVO
     * @param response
     */
    private void resp(ResponseVO responseVO,HttpServletResponse response){
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            String resp = JSONObject.toJSONString(responseVO);

            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-type", "application/json;charset=UTF-8");
            response.setHeader("Access-Control-Allow-Origin","*");
            response.setHeader("Access-Control-Allow-Credentials","true");
            response.setHeader("Access-Control-Allow-Methods","*");
            response.setHeader("Access-Control-Allow-Headers","*");
            response.setHeader("Access-Control-Max-Age","3600");

            writer = response.getWriter();
            writer.write(resp);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(writer != null){
                writer.checkError();
            }
        }
    }

}
