package com.lpw.joyfoodmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.entity.Category;
import com.lpw.joyfoodmall.mapper.CategoryMapper;
import com.lpw.joyfoodmall.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public IPage<Category> getCategoryPage(Page<Category> page, String searchField, String searchText) {
        LambdaQueryWrapper< Category> queryWrapper = new LambdaQueryWrapper<>();
        if (searchField != null && searchText != null) {
            switch (searchField){
                case "categoryId" -> queryWrapper.like(Category::getCategoryId, searchText);
                case "categoryName" -> queryWrapper.like(Category::getCategoryName, searchText);
                case "categoryDescription" -> queryWrapper.like(Category::getDescription, searchText);
                default -> {
                    queryWrapper.like(Category::getCategoryId, searchText).or()
                            .like(Category::getCategoryName, searchText).or()
                            .like(Category::getDescription, searchText);
                }
            }
        }
        return baseMapper.selectPage(page, queryWrapper);
    }
}
