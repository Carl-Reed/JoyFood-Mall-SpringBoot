package com.lpw.joyfoodmall.entity.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;

    private String username;

    private String email;

    private String phone;

    private Integer enabled;

    private Integer accountNonExpired;

    private Integer accountNonLocked;

    private Integer credentialsNonExpired;

    private LocalDateTime createTime;

    private LocalDateTime lastLogin;

    private String avatar;
}
