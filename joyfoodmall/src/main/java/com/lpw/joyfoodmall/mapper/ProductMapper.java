package com.lpw.joyfoodmall.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.entity.Product;
import org.apache.ibatis.annotations.Param;

public interface ProductMapper extends BaseMapper<Product> {
    /**
     * 分页查询商品及其价格区间和总库存
     */
    IPage<Product> selectProductPage(Page<Product> page, @Param("ew") Wrapper<Product> queryWrapper);
}
