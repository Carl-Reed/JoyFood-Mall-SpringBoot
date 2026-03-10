package com.lpw.joyfoodmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lpw.joyfoodmall.component.SecurityUser;
import com.lpw.joyfoodmall.entity.User;
import com.lpw.joyfoodmall.service.CustomUserDetailsService;
import com.lpw.joyfoodmall.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {
    private final UserService userService;

    @Override
    public SecurityUser loadUserByUsername(String loginAccount) throws UsernameNotFoundException {

        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginAccount)
                .or()
                .eq(User::getPhone, loginAccount)
                .or()
                .eq(User::getEmail, loginAccount)
        );
        if (user == null){
            throw new UsernameNotFoundException("用户不存在");
        }

        return new SecurityUser(user);
    }
}
