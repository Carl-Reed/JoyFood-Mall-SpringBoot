package com.lpw.joyfoodmall.controller.admin;

import com.lpw.joyfoodmall.component.JwtTokenUtil;
import com.lpw.joyfoodmall.component.SecurityUser;
import com.lpw.joyfoodmall.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.lpw.joyfoodmall.common.Result;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SysLoginController {

    // 注入认证管理器，用于校验用户名密码
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/check-login")
    public Result<String> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 触发Spring Security用户名密码校验
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 校验通过：获取SecurityUser
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

            // 生成JWT Token
            String token = jwtTokenUtil.generateToken(securityUser);
            userService.updateLastLoginTime(securityUser.getUsername());

            return Result.success(token,200,"登录成功");

        } catch (AuthenticationException e) {
            return Result.error("用户名或密码错误：" + e.getMessage(),400);
        }
    }

    // 封装登录请求参数（前端传JSON：{"username":"xxx","password":"xxx"}）
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
