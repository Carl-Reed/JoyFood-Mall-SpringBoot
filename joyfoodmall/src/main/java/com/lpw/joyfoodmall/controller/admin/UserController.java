package com.lpw.joyfoodmall.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.component.PasswordGenerator;
import com.lpw.joyfoodmall.entity.DTO.PageParams;
import com.lpw.joyfoodmall.entity.Role;
import com.lpw.joyfoodmall.component.SecurityUser;
import com.lpw.joyfoodmall.entity.User;
import com.lpw.joyfoodmall.entity.DTO.UserDTO;
import com.lpw.joyfoodmall.entity.UserRole;
import com.lpw.joyfoodmall.service.UserRoleService;
import com.lpw.joyfoodmall.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;

    @GetMapping("info")
    public Result<SecurityUser> userInfo(){
        try {
            // 从 SecurityContextHolder 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                String username = ((UserDetails) authentication.getPrincipal()).getUsername();

                SecurityUser user = userService.selectByUsername(username);
                // 从set中获取用户角色名称列表
                List<String> roleNames = new ArrayList<>();
                for (Role role : user.getRoles()){
                    String roleName = role.getRoleName();
                    roleName = roleName.substring(5); // 去除前缀 "ROLE_"
                    roleNames.add(roleName);
                }
                user.setRoleNames(roleNames);
                // 移除密码字段，阻止返回密码字段
                user.setPassword(null);

                return Result.success(user);
            } else {
                return Result.error("未登录或认证信息无效");
            }
        } catch (Exception e) {
            return Result.error("用户信息获取失败：" + e.getMessage());
        }
    }

    @GetMapping("list")
    public Result<Map<String, Object>> userManage(
            PageParams params,
            @RequestParam(required = false) String searchField,
            @RequestParam(required = false) String searchText
    ) {
        // 创建 Page 对象，参数为当前页码和每页显示记录数
        Page<User> page = new Page<>(params.getPage(), params.getLimit());

        // 调用分页查询方法
        IPage<User> userPage = userService.getUserPage(page, searchField, searchText);
        IPage<UserDTO> userDTOPage = userPage.convert(user -> {
            UserDTO dto = new UserDTO();
            // 手动赋值，排除password字段
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setEnabled(user.getEnabled());
            dto.setAccountNonExpired(user.getAccountNonExpired());
            dto.setAccountNonLocked(user.getAccountNonLocked());
            dto.setCredentialsNonExpired(user.getCredentialsNonExpired());
            dto.setCreateTime(user.getCreateTime());
            dto.setLastLogin(user.getLastLogin() != null ? user.getLastLogin() : null);
            dto.setAvatar(user.getAvatar());

            return dto;
        });

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", userDTOPage.getTotal());
        result.put("rows", userDTOPage.getRecords());
        return Result.success(result);
    }

    @PostMapping("add")
    public Result<?> userAdd(@RequestBody User user) {
        try {
            // 加密密码
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword); // 设置加密后的密码
            userService.save(user);
            // 给新增的用户默认的普通用户权限
            UserRole userRole = new UserRole();
            userRole.setUserId(Math.toIntExact(user.getId()));
            userRole.setRoleId(4);
            userRoleService.save(userRole);
            return Result.message("角色创建成功");
        } catch (Exception e) {
            return Result.error("角色创建失败: " + e.getMessage());
        }
    }

    @PutMapping("update")
    public Result<?> userUpdate(@RequestBody User user,@AuthenticationPrincipal SecurityUser loginUser) {
        try {
            User oldUser = userService.getById(user.getId());
            if (oldUser == null) return Result.error("用户不存在");
            // 不修改密码
            user.setPassword(oldUser.getPassword());
            userService.updateById(user);
            // 判断是否修改自己的信息
            if (Objects.equals(user.getId(), loginUser.getId())){
                return Result.success(null,100,"修改自己信息成功，请重新登录");
            }
            return Result.message("用户信息更新成功");
        } catch (Exception e) {
            return Result.error("用户信息更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("delete/{id}")
    public Result<?> userDelete(@PathVariable Integer id,@AuthenticationPrincipal SecurityUser user) {
        try {
            if (user.getId().equals(id)){
                return Result.error("不能删除自己的信息！",400);
            }else{
                userService.removeById(id);
                return Result.message("用户删除成功");
            }
        } catch (Exception e) {
            return Result.error("用户删除失败: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROOT')")
    @PutMapping("resetPwd/{id}")
    public Result<?> resetPwd(@PathVariable Integer id) {
        try {
            // 获取用户信息
            User userInfo = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getId, id));
            if (userInfo == null) {
                return Result.error("用户不存在");
            }
            // 生成随机密码
            PasswordGenerator.PasswordGenerationResult result = passwordGenerator.generatePassword(16);

            userInfo.setPassword(result.getEncodedPassword()); // 密码设置为加密后的
            userService.updateById(userInfo);
            return Result.success(result.getPlainPassword());
        }catch (Exception e){
            return Result.error("密码重置失败: " + e.getMessage());
        }
    }

    @PostMapping("changePwd")
    public Result<?> changePwd(@RequestBody Map<String, String> params, @AuthenticationPrincipal SecurityUser loginUser) {
        LambdaQueryWrapper< User> queryWrapper = new LambdaQueryWrapper<>();
        User user = userService.getOne(queryWrapper.eq(User::getUsername, loginUser.getUsername()));
        if (user == null){
            return Result.error("用户不存在");
        }
        if (!passwordEncoder.matches(params.get("currentPassword"), user.getPassword())){
            return Result.error("当前密码错误");
        }else if (!params.get("newPassword").equals(params.get("confirmPassword"))){
            return Result.error("新密码不一致");
        }else {
            user.setPassword(passwordEncoder.encode(params.get("newPassword")));
            userService.updateById(user);
            return Result.message("密码修改成功");
        }
    }

}
