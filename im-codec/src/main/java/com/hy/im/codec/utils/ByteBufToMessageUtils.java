package com.hy.im.codec.utils;

import com.alibaba.fastjson.JSONObject;
import com.hy.im.codec.proto.Message;
import com.hy.im.codec.proto.MessageHeader;
import io.netty.buffer.ByteBuf;

/**
 * @ClassName ByteBufToMessageUtils
 * description:将ByteBuf转化为Message实体，根据私有协议转换
 *                私有协议规则，
 *                4位表示Command表示消息的开始，
 *                4位表示version
 *                4位表示clientType
 *                4位表示messageType
 *                4位表示appId
 *                4位表示imei长度
 *                imei
 *                4位表示数据长度
 *                data
 *                后续将解码方式加到数据头根据不同的解码方式解码，如pb，json，现在用json字符串
 * yao create 2023年06月28日
 * version: 1.0
 */
public class ByteBufToMessageUtils {

    public static Message transition(ByteBuf in){
        // 指令
        int command = in.readInt();
        // 版本
        int version = in.readInt();
        // clientType
        int clientType = in.readInt();
        // 消息解析类型
        int messageType =in.readInt();
        // appid
        int appId =in.readInt();
        // imei 长度
        int imeiLength =in.readInt();
        // 请求体长度
        int bodyLen =in.readInt();
        if(in.readableBytes()<imeiLength+bodyLen){
            in.resetReaderIndex();
            return null;
        }
        byte[] imeiData = new byte[imeiLength];
        in.readBytes(imeiData);
        String imei = new String(imeiData);

        // 请求体
        byte[] bodyData = new byte[bodyLen];
        in.readBytes(bodyData);

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCommand(command);
        messageHeader.setVersion(version);
        messageHeader.setClientType(clientType);
        messageHeader.setAppId(appId);
        messageHeader.setImei(imei);
        messageHeader.setLength(bodyLen);
        messageHeader.setMessageType(messageType);

        Message message = new Message();
        message.setMessageHeader(messageHeader);

        if(messageType == 0x0){
            String body = new String(bodyData);
            JSONObject json =JSONObject.parseObject(body);
            message.setMessagePack(json);
        }

        in.resetReaderIndex();
        return message;

    }


}
