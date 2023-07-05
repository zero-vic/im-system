package com.hy.im.service.message.controller;

import com.hy.im.common.model.SyncReq;
import com.hy.im.common.model.message.CheckSendMessageReq;
import com.hy.im.common.response.ResponseVO;
import com.hy.im.service.message.model.req.SendMessageReq;
import com.hy.im.service.message.service.MessageSyncService;
import com.hy.im.service.message.service.P2PMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName MessageController
 * description: 管理员发送消息的接口
 * yao create 2023年07月04日
 * version: 1.0
 */
@RestController
@RequestMapping("v1/message")
public class MessageController {
    @Autowired
    private P2PMessageService p2PMessageService;

    @Autowired
    private MessageSyncService messageSyncService;

    /**
     * 发送信息接口
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/send")
    public ResponseVO send(@RequestBody @Validated SendMessageReq req, Integer appId)  {
        req.setAppId(appId);
        return ResponseVO.successResponse(p2PMessageService.send(req));
    }

    /**
     * 校验信息接口
     * @param req
     * @return
     */
    @RequestMapping("/checkSend")
    public ResponseVO checkSend(@RequestBody @Validated CheckSendMessageReq req)  {
        return p2PMessageService.imServerPermissionCheck(req.getFromId(),req.getToId()
                ,req.getAppId());
    }

    /**
     * 同步离线消息
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/syncOfflineMessage")
    public ResponseVO syncOfflineMessage(@RequestBody
                                         @Validated SyncReq req, Integer appId)  {
        req.setAppId(appId);
        return messageSyncService.syncOfflineMessage(req);
    }
}
