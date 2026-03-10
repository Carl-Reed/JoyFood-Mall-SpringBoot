package com.lpw.joyfoodmall.entity.DTO;

import lombok.Data;

// 存放权限表信息
@Data
public class UserRoleDTO {
    private Integer userId;
    private String username;
    private Integer roleId;
    private String roleName;
    private Integer roleIds[];
}
