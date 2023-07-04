package com.hy.im.tcp.feign;

import com.hy.im.common.model.message.CheckSendMessageReq;
import com.hy.im.common.response.ResponseVO;
import feign.Headers;
import feign.RequestLine;

/**
 * 调用消息服务的feign接口
 */
public interface FeignMessageService {
    @Headers({"Content-Type: application/json","Accept: application/json"})
    @RequestLine("POST /message/checkSend")
    public ResponseVO checkSendMessage(CheckSendMessageReq o);
}
