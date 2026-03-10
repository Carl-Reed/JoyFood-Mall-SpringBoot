package com.lpw.joyfoodmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.entity.Role;
import com.lpw.joyfoodmall.mapper.RoleMapper;
import com.lpw.joyfoodmall.service.RoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public IPage<Role> getRolePage(Page<Role> page, String searchField, String searchText) {
        // 根据搜索字段和搜索文本进行查询
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (searchText != null && !searchText.isEmpty()) {
            switch (searchField) {
                case "id" -> wrapper.like(Role::getId, searchText);
                case "roleName" -> wrapper.like(Role::getRoleName, searchText);
                case "roleDesc" -> wrapper.like(Role::getRoleDesc, searchText);
                default -> wrapper.like(Role::getId, searchText).or()
                        .like(Role::getRoleName, searchText).or()
                        .like(Role::getRoleDesc, searchText);
            }
        }
        return baseMapper.selectPage(page, wrapper);
    }
}
