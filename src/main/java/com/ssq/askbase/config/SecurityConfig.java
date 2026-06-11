package com.ssq.askbase.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssq.askbase.common.enums.ErrorCode;
import com.ssq.askbase.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //关闭csrf
       http.csrf(csrf -> csrf.disable())
               //关闭表单登录
               .formLogin(form -> form.disable())
               //关闭Basic 认证
               .httpBasic(basic -> basic.disable())
               //设置 Session 策略为无状态。Spring Security 不创建 Session，也不使用 Session 保存登录状态。
               .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .authorizeHttpRequests(auth -> auth
                       .requestMatchers("/api/health").permitAll()
                       //表示除了上面明确放行的接口，其他所有接口都必须认证后才能访问。
                       .anyRequest().authenticated()
               )
               //配置认证异常处理
               /*
                比如访问：
                GET /api/kbs
                但没有携带有效 Token，就会进入这里。
                */
               .exceptionHandling(exception -> exception
                       .authenticationEntryPoint((request, response, authException) -> {
                           //返回 401
                           response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                           //设置响应内容类型为 JSON，并指定 UTF-8 编码。
                           //如果不设置，前端可能无法正确识别返回内容，中文错误信息也可能乱码。
                           response.setContentType("application/json;charset=UTF-8");
                           response.getWriter().write(
                                   //objectMapper.writeValueAsString(...) 的作用是把 Java 对象转成 JSON 字符串。
                                   objectMapper.writeValueAsString(
                                   ApiResponse.fail(ErrorCode.UNAUTHORIZED)
                           ));
                       })
               );

        return http.build();

    }


}
