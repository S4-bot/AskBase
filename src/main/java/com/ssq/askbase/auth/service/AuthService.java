package com.ssq.askbase.auth.service;

import com.ssq.askbase.auth.dto.LoginRequest;
import com.ssq.askbase.auth.dto.RegisterRequest;
import com.ssq.askbase.auth.vo.LoginVo;
import com.ssq.askbase.auth.vo.UserInfoVo;


public interface AuthService {

    void register(RegisterRequest registerRequest);

    LoginVo login(LoginRequest loginRequest);

    UserInfoVo getCurrentUser();


}
