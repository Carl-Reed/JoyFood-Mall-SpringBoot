package com.lpw.joyfoodmall.utils;

import com.lpw.joyfoodmall.component.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    /** 获取用户名 */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "System";
    }

    /** 获取用户ID */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            // LoginUser 是你自定义的实现 UserDetails 的类
            return ((SecurityUser) authentication.getPrincipal()).getId();
        }
        // 如果是测试环境或未登录，返回空或报错
        return null;
    }


}
