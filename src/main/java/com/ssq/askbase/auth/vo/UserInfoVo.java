package com.ssq.askbase.auth.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoVo {

    private Long id;

    private String username;

    private Integer status;
}
