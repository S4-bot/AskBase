package com.ssq.askbase.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ssq.askbase.auth.dto.LoginRequest;
import com.ssq.askbase.auth.dto.RegisterRequest;
import com.ssq.askbase.auth.entity.User;
import com.ssq.askbase.auth.mapper.UserMapper;
import com.ssq.askbase.auth.security.JwtTokenProvider;
import com.ssq.askbase.auth.service.AuthService;
import com.ssq.askbase.auth.vo.LoginVo;
import com.ssq.askbase.auth.vo.UserInfoVo;
import com.ssq.askbase.common.enums.ErrorCode;
import com.ssq.askbase.common.exception.BusinessException;
import com.ssq.askbase.common.util.UserContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService {

    private static final int USER_STATUS_NORMAL = 1;

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        String username = registerRequest.getUsername().trim();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username);
        User user = baseMapper.selectOne(queryWrapper);
        if (user != null) {
            throw new BusinessException(ErrorCode.USER_EXIST);
        }

        LocalDateTime now = LocalDateTime.now();
        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .status(USER_STATUS_NORMAL)
                .created(now)
                .updated(now)
                .build();
        baseMapper.insert(newUser);
    }

    @Override
    public LoginVo login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername().trim();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username);
        User user = baseMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!isNormalUser(user)) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return LoginVo.builder()
                .token(token)
                .tokenType("Bearer")
                .expireSeconds(jwtTokenProvider.getExpireSeconds())
                .user(toUserInfoVo(user))
                .build();
    }

    @Override
    public UserInfoVo getCurrentUser() {
        Long userId = UserContextHolder.getUserId();
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!isNormalUser(user)) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        return toUserInfoVo(user);
    }

    private boolean isNormalUser(User user) {
        return user.getStatus() != null && user.getStatus() == USER_STATUS_NORMAL;
    }

    private UserInfoVo toUserInfoVo(User user) {
        return UserInfoVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .status(user.getStatus())
                .build();
    }
}
