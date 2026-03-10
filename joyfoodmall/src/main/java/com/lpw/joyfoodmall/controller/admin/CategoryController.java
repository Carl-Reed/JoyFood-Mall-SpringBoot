package com.lpw.joyfoodmall.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.entity.Category;
import com.lpw.joyfoodmall.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("list")
    public Result<?> categoryList(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) Integer isEnable)
    {
        try {
            // 构建查询条件
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();

            // 如果搜索内容不为空，则进行模糊查询
            if (StringUtils.hasText(searchText)) {
                wrapper.like(Category::getCategoryName, searchText);
            }

            // 状态筛选
            if (isEnable != null) {
                wrapper.eq(Category::getIsEnable, isEnable);
            }

            // 排序
            wrapper.orderByAsc(Category::getSortOrder);

            // 执行查询，返回全量 List
            List<Category> list = categoryService.list(wrapper);

            return Result.success(list);

        } catch (Exception e) {
            return Result.error("获取分类列表失败: " + e.getMessage());
        }
    }

    @GetMapping("categoryList")
    public Result<?> getCategoryList() {
        try {
            return Result.success(categoryService.list());
        } catch (Exception e) {
            return Result.error("获取类别列表失败: " + e.getMessage());
        }
    }

    @GetMapping("getCategoryName/{categoryId}")
    public Map<String, Object> getCategoryName(@PathVariable Integer categoryId) {
        String categoryName = categoryService.getById(categoryId).getCategoryName();
        Map<String, Object> result = new HashMap<>();
        result.put("categoryName", categoryName);
        return result;
    }

    @PostMapping("add")
    public Result<?> addCreate(@RequestBody Category category) {
        try {
            categoryService.save(category);
            return Result.message("类别创建成功");
        } catch (Exception e) {
            return Result.error("类别创建失败！");
        }
    }

    @PutMapping("edit")
    public Result<?> editCategory(@RequestBody Category category) {
        try {
            categoryService.updateById(category);
            return Result.message("类别更新成功");
        } catch (Exception e) {
            return Result.error("类别更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("delete/{categoryId}")
    public Result<?> delCategory(@PathVariable Integer categoryId){
        try{
            categoryService.removeById(categoryId);
            return Result.message("类别删除成功");
        }catch (Exception e){
            return Result.error("类别删除失败：" + e.getMessage());
        }
    }

}
