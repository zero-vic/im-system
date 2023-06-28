package com.hy.im.common.enums;

import com.hy.im.common.exception.ApplicationExceptionEnum;

/**
 * Description: 用户错误枚举类
 * yao create
 **/
public enum UserErrorCode implements ApplicationExceptionEnum {


    IMPORT_SIZE_BEYOND(20000,"导入數量超出上限"),
    USER_IS_NOT_EXIST(20001,"用户不存在"),
    SERVER_GET_USER_ERROR(20002,"服务获取用户失败"),
    MODIFY_USER_ERROR(20003,"更新用户失败"),
    SERVER_NOT_AVAILABLE(71000, "没有可用的服务");
    private int code;
    private String error;

    UserErrorCode(int code, String error){
        this.code = code;
        this.error = error;
    }
    @Override
    public int getCode() {
        return this.code;
    }
    @Override
    public String getError() {
        return this.error;
    }

}
