package com.ssq.askbase.auth.controller;

import com.ssq.askbase.auth.dto.LoginRequest;
import com.ssq.askbase.auth.dto.RegisterRequest;
import com.ssq.askbase.auth.service.AuthService;
import com.ssq.askbase.auth.vo.LoginVo;
import com.ssq.askbase.auth.vo.UserInfoVo;
import com.ssq.askbase.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping({ "/api/askbase"})
public class AuthController {

    private final AuthService authService;

   @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest registerRequest){

       authService.register(registerRequest);
       return ApiResponse.success();
   }

   @PostMapping("/login")
    public ApiResponse<LoginVo> login(@Valid @RequestBody LoginRequest loginRequest){
        return ApiResponse.success(authService.login(loginRequest));
   }

   @GetMapping("/me")
    public ApiResponse<UserInfoVo> getCurrentUser(){
       return ApiResponse.success(authService.getCurrentUser());
   }

}
