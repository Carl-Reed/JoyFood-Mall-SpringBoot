package com.lpw.joyfoodmall.component;

import com.lpw.joyfoodmall.entity.Role;
import com.lpw.joyfoodmall.entity.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class SecurityUser implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private Integer enabled = 1;
    private Integer accountNonExpired = 1;
    private Integer accountNonLocked = 1;
    private Integer credentialsNonExpired = 1;
    private LocalDateTime createTime;
    private LocalDateTime lastLogin;
    private Set<Role> roles = new HashSet<>();
    private List<String> roleNames;
    private User user;

    public SecurityUser(){

    }

    public SecurityUser(User user) {
        if (user != null) {
            this.user = user;
            this.id = user.getId();
            this.username = user.getUsername();
            this.password = user.getPassword();
            this.enabled = user.getEnabled();
            this.roles = user.getRoles();

            this.accountNonExpired = 1;
            this.accountNonLocked = 1;
            this.credentialsNonExpired = 1;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // 判断账户是否未过期，1 表示未过期，0 表示已过期
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired != null && accountNonExpired == 1;
    }

    // 判断账户是否未锁定，1 表示未锁定，0 表示已锁定
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked != null && accountNonLocked == 1;
    }

    // 判断凭证（密码）是否未过期，1 表示未过期，0 表示已过期
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired != null && credentialsNonExpired == 1;
    }

    // 判断账户是否启用，1 表示启用，0 表示禁用
    @Override
    public boolean isEnabled() {
        return enabled != null && enabled == 1;
    }

}
