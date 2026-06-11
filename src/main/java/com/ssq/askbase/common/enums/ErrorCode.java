package com.ssq.askbase.common.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "成功"),

    PARAM_ERROR(400, "请求参数错误"),
    UNAUTHORIZED(401,"未登录"),
    FORBIDDEN(403,"无权限"),
    NOT_FOUND(404, "请求资源不存在"),

    BUSINESS_ERROR(5001, "业务异常"),
    SYSTEM_ERROR(5002, "系统异常"),

    // 用户相关错误码 (51xx 系列)
    USER_EXIST(5100, "用户名已存在"),
    USER_NOT_FOUND(5101, "用户不存在"),
    PASSWORD_ERROR(5102, "密码错误"),
    ACCOUNT_DISABLED(5103, "账号已被禁用"),
    USER_NOT_LOGIN(5104, "用户未登录");


    private final Integer code;
    private final String message;




}
