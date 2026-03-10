package com.lpw.joyfoodmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.component.SecurityUser;
import com.lpw.joyfoodmall.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lpw.joyfoodmall.entity.DTO.UserRoleDTO;

public interface UserService extends IService<User> {
    SecurityUser selectByUsername(String username);
    void updateLastLoginTime(String username);
    IPage<UserRoleDTO> getUserRolePage(Page<UserRoleDTO> page, String searchField, String searchText);
    IPage<User> getUserPage(Page<User> page, String searchField, String searchText);
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
}
