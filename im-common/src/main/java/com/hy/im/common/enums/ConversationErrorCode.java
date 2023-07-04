package com.hy.im.common.enums;


import com.hy.im.common.exception.ApplicationExceptionEnum;


public enum ConversationErrorCode implements ApplicationExceptionEnum {

    CONVERSATION_UPDATE_PARAM_ERROR(50000,"会话修改参数错误"),


    ;

    private int code;
    private String error;

    ConversationErrorCode(int code, String error){
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
