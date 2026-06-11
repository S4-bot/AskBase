package com.ssq.askbase.common.response;

import com.ssq.askbase.common.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    //业务状态码
    private Integer code;
    //提示信息
    private String message;
    //返回的数据
    private T data;

    public static <T> ApiResponse<T> success(T data){
        return new ApiResponse(ErrorCode.SUCCESS.getCode(),ErrorCode.SUCCESS.getMessage(),data);
    }

    public static <T> ApiResponse<T> fail(Integer code,String message){
        return new ApiResponse<>(code,message,null);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode){
        return new ApiResponse<>(errorCode.getCode(),errorCode.getMessage(),null);
    }


}
