package com.lpw.joyfoodmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpw.joyfoodmall.component.SecurityUser;
import com.lpw.joyfoodmall.entity.DTO.UserRoleDTO;
import com.lpw.joyfoodmall.entity.Role;
import com.lpw.joyfoodmall.entity.User;
import com.lpw.joyfoodmall.mapper.RoleMapper;
import com.lpw.joyfoodmall.mapper.UserMapper;
import com.lpw.joyfoodmall.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SecurityUser selectByUsername(String username) {
        // 查询用户基本信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = getBaseMapper().selectOne(queryWrapper);

        if (user == null) return null;

        // 根据用户 ID 查询角色
        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>()
                        .inSql(Role::getId, "SELECT role_id FROM user_roles WHERE user_id = " + user.getId())
        );

        // 构建 SecurityUser 对象
        SecurityUser securityUser = new SecurityUser();

        // 复制基本属性
        securityUser.setId(user.getId());
        securityUser.setUsername(user.getUsername());
        securityUser.setPassword(user.getPassword());
        securityUser.setPhone(user.getPhone());
        securityUser.setEmail(user.getEmail());
        securityUser.setAvatar(user.getAvatar());
        securityUser.setEnabled(user.getEnabled());
        securityUser.setAccountNonExpired(user.getAccountNonExpired());
        securityUser.setAccountNonLocked(user.getAccountNonLocked());
        securityUser.setCredentialsNonExpired(user.getCredentialsNonExpired());

        // 设置角色
        securityUser.setRoles(new HashSet<>(roles));
        securityUser.setRoles(new HashSet<>(roles));
        return securityUser;
    }

    @Override
    @Transactional
    public void updateLastLoginTime(String username) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUsername, username)  // 使用方法引用替代字符串字段名
                .set(User::getLastLogin, LocalDateTime.now());  // 设置 lastLogin 字段

        this.update(updateWrapper);
    }

    @Override
    public IPage<UserRoleDTO> getUserRolePage(Page<UserRoleDTO> page, String searchField, String searchText) {
        // 构建查询条件
        QueryWrapper<UserRoleDTO> queryWrapper = new QueryWrapper<>();

        if (searchField != null && searchText != null) {
            switch (searchField) {
                case "username" -> queryWrapper.like("u.username", searchText);
                case "roleName" -> queryWrapper.like("r.role_name", searchText);
                case "userId" -> queryWrapper.like("u.id", searchText);
                default -> {
                    queryWrapper.like("u.id", searchText).or()
                            .like("u.username", searchText).or()
                            .like("r.role_name", searchText);
                }
            }
        }
        // 执行分页查询
        return baseMapper.selectUserRolePage(page, queryWrapper);
    }

    @Override
    public IPage<User> getUserPage(Page<User> page, String searchField, String searchText) {
        LambdaQueryWrapper< User> queryWrapper = new LambdaQueryWrapper<>();

        if (searchField != null && searchText != null) {
            switch (searchField) {
                case "id" -> queryWrapper.like(User::getId, searchText);
                case "username" -> queryWrapper.like(User::getUsername, searchText);
                case "email" -> queryWrapper.like(User::getEmail, searchText);
                case "phone" -> queryWrapper.like(User::getPhone, searchText);
                default -> {
                    queryWrapper.like(User::getId, searchText).or().like(User::getUsername, searchText).or()
                            .like(User::getEmail, searchText).or().like(User::getPhone, searchText);
                }
            }
        }

        return baseMapper.selectPage(page,queryWrapper);

    }

    // 修改用户密码
    public boolean updatePassword(Long userId, String oldPassword, String newPassword){
        // 根据 ID 查询数据库中的用户信息
        User user = this.getById(userId);
        if (user == null) {
            return false;
        }

        // 校验旧密码是否匹配
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            // 原密码输入错误
            return false;
        }

        // 加密新密码
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        // 更新数据库
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(encodedNewPassword);

        return this.updateById(updateUser);
    }
}
