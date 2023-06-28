package com.hy.im.codec.decoder;

import com.hy.im.codec.proto.Message;
import com.hy.im.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;


/**
 * @ClassName MessageDecoder
 * description: 消息解码类
 * 私有协议 : 请求头（指令 版本 clientType 消息解析类型 imei长度 appId bodylen）+ imei号 + 请求体
 * 请求头每个4个字节  28 + imei + body
 * yao create 2023年06月28日
 * version: 1.0
 */
public class MessageDecoder extends ByteToMessageDecoder {


    /**
     *      28 + imei + body
     *      请求头（指令 版本 clientType 消息解析类型 appId imei长度 bodylen）+ imei号 + 请求体
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        // 读到的字节小于28直接终止
        if(in.readableBytes()< 28){
            return;
        }
        Message message = ByteBufToMessageUtils.transition(in);
        if (message == null){
            return;
        }
        out.add(message);

    }



}
