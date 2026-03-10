package com.lpw.joyfoodmall.controller.mall;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.entity.User;
import com.lpw.joyfoodmall.service.ImageService;
import com.lpw.joyfoodmall.service.UserService;
import com.lpw.joyfoodmall.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final UserService userService;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;

    // 上传头像
    @PostMapping("avatar/upload")
    public Result<?> uploadAvatar(
            @RequestParam("id") Integer id,
            @RequestParam("file") MultipartFile file) {
        try {
            imageService.saveAvatar(id, file);
            return Result.message("头像修改成功");
        } catch (IOException e) {
            return Result.error("头像修改失败");
        }
    }

    @PutMapping("/update")
    public Result<?> updateInfo(@RequestBody User user) {
        // 仅允许修改昵称等基础字段
        if (user.getId().equals(SecurityUtils.getCurrentUserId())){
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setUsername(user.getUsername());
            updateUser.setEmail(user.getEmail());
            updateUser.setPhone(user.getPhone());
            userService.updateById(updateUser);
            return Result.message("资料更新成功");
        }else {
            return Result.error("资料更新失败");
        }
    }

    @PutMapping("/updatePassword")
    public Result<?> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        boolean success = userService.updatePassword(SecurityUtils.getCurrentUserId(), oldPassword, newPassword);
        return success ? Result.message("密码修改成功") : Result.error("密码修改失败，检查原密码是否正确");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        // 检查用户名是否存在
        long count = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (count > 0) return Result.error("用户名已存在");

        // 密码加密
        String hashedPassword = passwordEncoder.encode(user.getPassword());

        // 保存用户
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPhone(user.getPhone());
        newUser.setPassword(hashedPassword);
        newUser.setEmail(user.getEmail());

        userService.save(newUser);
        return Result.success("注册成功");
    }
}
