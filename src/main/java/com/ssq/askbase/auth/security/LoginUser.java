package com.ssq.askbase.auth.security;

import com.ssq.askbase.common.util.CurrentUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginUser implements CurrentUser {

    private Long userId;

    private String username;
}
