package com.hy.im.service.user.controller;

import com.hy.im.common.enums.ClientTypeEnum;
import com.hy.im.common.response.ResponseVO;
import com.hy.im.common.route.RouteHandle;
import com.hy.im.common.route.RouteInfo;
import com.hy.im.common.utils.RouteInfoParseUtil;
import com.hy.im.service.user.model.LoginReq;
import com.hy.im.service.util.ZKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ImUserController
 * description:
 * yao create 2023年06月30日
 * version: 1.0
 */
@RestController
@RequestMapping("v1/user")
public class ImUserController {
    @Autowired
    private RouteHandle routeHandle;

    @Autowired
    private ZKit zKit;

    /**
     * @param
     * @return com.lld.im.common.ResponseVO
     * @description im的登录接口，返回im地址
     * @author chackylee
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ResponseVO login(@RequestBody LoginReq req) {
//        req.setAppId(appId);

//        ResponseVO login = imUserService.login(req);
        System.out.println(req.toString());
        if (true) {
            List<String> allNode = new ArrayList<>();
            if (req.getClientType() == ClientTypeEnum.WEB.getCode()) {
                allNode = zKit.getAllWebNode();
            } else {
                allNode = zKit.getAllTcpNode();
            }
            String s = routeHandle.routeServer(allNode, req
                    .getUserId());
            RouteInfo parse = RouteInfoParseUtil.parse(s);
            return ResponseVO.successResponse(parse);
        }

        return ResponseVO.errorResponse();
    }

}
