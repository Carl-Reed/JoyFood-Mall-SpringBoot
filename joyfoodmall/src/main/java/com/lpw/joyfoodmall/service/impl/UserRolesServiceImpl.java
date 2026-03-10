package com.lpw.joyfoodmall.service.impl;

import com.lpw.joyfoodmall.entity.UserRole;
import com.lpw.joyfoodmall.mapper.UserRoleMapper;
import com.lpw.joyfoodmall.service.UserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRolesServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
}
