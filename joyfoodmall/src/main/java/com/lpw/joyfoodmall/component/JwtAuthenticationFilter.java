package com.lpw.joyfoodmall.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.service.CustomUserDetailsService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 打印请求路径和Authorization头（看是否是登录请求/Token是否传递）
        String requestUri = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        // 登录请求直接放行（不需要解析Token）
        if ("/check-login".equals(requestUri) && "POST".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;
        String username = null;

        // 提取Token（严格处理Bearer前缀）
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 截取前缀并去除首尾空格（避免前端传多余空格）
            token = authHeader.substring(7).trim();
            // 前置校验：Token是否为空/格式非法
            if (token.isEmpty() || token.chars().filter(c -> c == '.').count() != 2) {
                sendErrorResponse(response, "Token格式错误（空值/无有效分隔符）");
                return;
            }
        } else {
            // 无Authorization头/格式错误，直接放行（让后续过滤器处理）
            filterChain.doFilter(request, response);
            return;
        }

        // 解析Token（仅当Token有效且未认证时）
        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // 解析用户名
                username = jwtTokenUtil.getUsernameFromToken(token);

                // 验证Token并设置认证信息
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
                        sendErrorResponse(response, "账号状态异常，请联系管理员");
                        return;
                    }

                    if (jwtTokenUtil.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        sendErrorResponse(response, "Token验证失败（签名/过期）");
                        return;
                    }
                }
            }
        } catch (MalformedJwtException e) {
            sendErrorResponse(response, "Token格式错误：" + e.getMessage());
            return;
        } catch (Exception e) {
            sendErrorResponse(response, "Token解析失败：" + e.getMessage());
            return;
        }

        // 放行请求
        filterChain.doFilter(request, response);
    }

    // 辅助方法：返回统一的错误响应
    private void sendErrorResponse(HttpServletResponse response, String msg) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(msg,401)));
    }
}