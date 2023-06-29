package com.hy.im.common.enums;

/**
 * @ClassName ClientTypeEnum
 * description: 客户端类型枚举类
 * yao create 2023年06月29日
 * version: 1.0
 */
public enum ClientTypeEnum {
    /**
     * 客户端类型
     */
    WEBAPI(0,"webApi"),
    WEB(1,"web"),
    IOS(2,"ios"),
    ANDROID(3,"android"),
    WINDOWS(4,"windows"),
    MAC(5,"mac"),
            ;
    private int code;
    private String msg;

    ClientTypeEnum(int code, String msg){
        this.code = code;
        this.msg = msg;
    }
    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
