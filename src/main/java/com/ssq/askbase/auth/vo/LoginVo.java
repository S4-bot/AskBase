package com.ssq.askbase.auth.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVo {

    private String token;

    private String tokenType;

    private Long expireSeconds;

    private UserInfoVo user;

}
