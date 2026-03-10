package com.lpw.joyfoodmall.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lpw.joyfoodmall.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lpw.joyfoodmall.entity.DTO.UserRoleDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


public interface UserMapper extends BaseMapper<User> {

    @Select("""
        SELECT u.id AS userId,u.username,r.id AS roleId,r.role_name
        FROM users u
        LEFT JOIN user_roles ur ON u.id=ur.user_id
        LEFT JOIN roles r ON r.id=ur.role_id
        ${ew.customSqlSegment}
        ORDER BY u.id
    """)
    IPage<UserRoleDTO> selectUserRolePage(IPage<UserRoleDTO> page,
                                          @Param("ew") QueryWrapper<UserRoleDTO> queryWrapper);
}
