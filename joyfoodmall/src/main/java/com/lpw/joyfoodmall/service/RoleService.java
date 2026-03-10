package com.lpw.joyfoodmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lpw.joyfoodmall.entity.Role;

public interface RoleService extends IService<Role> {
    IPage<Role> getRolePage(Page<Role> page, String searchField, String searchText);
}
