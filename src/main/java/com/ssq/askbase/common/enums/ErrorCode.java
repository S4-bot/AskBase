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
    SYSTEM_ERROR(5002, "系统异常");



    private final Integer code;
    private final String message;




}
