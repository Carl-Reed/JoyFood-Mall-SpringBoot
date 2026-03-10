package com.lpw.joyfoodmall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.entity.DTO.PageParams;
import com.lpw.joyfoodmall.entity.Role;
import com.lpw.joyfoodmall.entity.UserRole;
import com.lpw.joyfoodmall.entity.DTO.UserRoleDTO;
import com.lpw.joyfoodmall.service.RoleService;
import com.lpw.joyfoodmall.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/role")
public class RoleController {
    private final RoleService roleService;
    private final UserRoleService userRoleService;

    // 分页查询角色列表
    @GetMapping("list")
    public Result<Map<String, Object>> roleManage(
            PageParams params,
            @RequestParam(required = false) String searchField,
            @RequestParam(required = false) String searchText
    ) {
        Page<Role> page = new Page<>(params.getPage(), params.getLimit());

        IPage<Role> pageResult = roleService.getRolePage(page, searchField, searchText);

        Map<String, Object> result = new HashMap<>();
        result.put("total", pageResult.getTotal());
        result.put("rows", pageResult.getRecords());
        return Result.success(result);
    }

    @PostMapping("add")
    public Result<?> roleAdd(@RequestBody Role role){
        try {
            roleService.save(role);
            return Result.message("角色创建成功");
        } catch (Exception e) {
            return Result.error("角色创建失败: " + e.getMessage());
        }
    }

    @PutMapping("update")
    public Result<?> roleUpdate(@RequestBody Role role){
        try {
            roleService.updateById(role);
            return Result.message("角色更新成功");
        } catch (Exception e) {
            return Result.error("角色更新失败: " + e.getMessage());
        }
    }

    @GetMapping("list/all")
    public Result<?> roleList(){
        try {
            return Result.success(roleService.list());
        }catch (Exception e){
            return Result.error("获取角色列表失败: "+ e.getMessage());
        }
    }

    @GetMapping("idList/{id}")
    public Result<?> roleIdList(@PathVariable Integer id){
        try {
            List<UserRole> list = userRoleService.list(
                    new LambdaQueryWrapper<UserRole>()
                            .eq(UserRole::getUserId, id)
            );
            List<Integer> roleIds = list.stream()
                    .map(UserRole::getRoleId) // 获取每条记录的 roleId
                    .collect(Collectors.toList());
            return Result.success(roleIds);
        } catch (Exception e) {
            return Result.error("获取角色id列表失败: " + e.getMessage());
        }
    }

    @PostMapping("userRole/save")
    @Transactional // 开启事务，保证原子性
    public Result<?> saveUserRoles(@RequestBody UserRoleDTO dto) {
        Integer userId = dto.getUserId();
        List<Integer> roleIds = List.of(dto.getRoleIds()); // 假设 DTO 里已经是 List<Integer>

        try {
            // 删除用户所拥有的所有角色 SQL: DELETE FROM user_roles WHERE user_id = ?
            userRoleService.remove(new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getUserId, userId));

            // 批量插入新角色
            if (!roleIds.isEmpty()) {
                // 将 roleId 列表转换为 UserRole 实体对象列表
                List<UserRole> userRoles = roleIds.stream().map(roleId -> {
                    UserRole ur = new UserRole();
                    ur.setUserId(userId);
                    ur.setRoleId(roleId);
                    return ur;
                }).collect(Collectors.toList());

                // 保存最新的角色列表
                userRoleService.saveBatch(userRoles);
            }

            return Result.message("角色授权成功");
        } catch (Exception e) {
            return Result.error("授权失败：" + e.getMessage());
        }
    }

}
