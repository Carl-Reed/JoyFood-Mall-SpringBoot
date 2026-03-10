package com.lpw.joyfoodmall.controller.mall;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.entity.Category;
import com.lpw.joyfoodmall.entity.DTO.MallProductDTO;
import com.lpw.joyfoodmall.entity.DTO.PageParams;
import com.lpw.joyfoodmall.entity.DTO.ProductSaveDTO;
import com.lpw.joyfoodmall.entity.Product;
import com.lpw.joyfoodmall.entity.SkuStock;
import com.lpw.joyfoodmall.service.CategoryService;
import com.lpw.joyfoodmall.service.ProductService;
import com.lpw.joyfoodmall.service.SkuStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mall/product")
public class MallProductController {

    private final ProductService productService;
    private final SkuStockService skuStockService;
    private final CategoryService categoryService;

    // 获取新品列表
    @GetMapping("/new-list")
    public Result<List<MallProductDTO>> getNewProducts() {
        List<Product> list = productService.getNewArrivals(8);

        List<MallProductDTO> dtoList = list.stream().map(this::convertToDTO).toList();
        return Result.success(dtoList);
    }

    // 获取今日推荐商品
    @GetMapping("/today-recommend")
    public Result<List<MallProductDTO>> getTodayRecommend() {
        // 筛选条件：1.已上架 2.是推荐商品 3.按更新时间倒序取前4个
        List<Product> list = productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getIsPublish, 1)
                .eq(Product::getIsRecommend, 1)
                .orderByDesc(Product::getUpdateTime)
                .last("LIMIT 4")
        );
        List<MallProductDTO> dtoList = list.stream().map(this::convertToDTO).toList();
        return Result.success(dtoList);
    }

    // 获取商品列表
    @GetMapping("/list")
    public Result<IPage<MallProductDTO>> getProductList(
            PageParams params, String keyword, Long categoryId, Integer sort) {
        Page<Product> page = new Page<>(params.getPage(), params.getLimit());

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 匹配上架商品
        wrapper.eq(Product::getIsPublish, 1);
        // 若有关键词则模糊匹配
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Product::getProductName, keyword)
                    .or()
                    .like(Product::getProductName, keyword));
        }
        // 若传来分类id，则按分类查询
        if (categoryId != null) {
            // 查出所有子分类对象
            LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
            categoryWrapper.eq(Category::getParentId, categoryId);
            List<Category> children = categoryService.list(categoryWrapper);

            // 提取出子分类的 ID 集合
            List<Long> idList = children.stream()
                    .map(Category::getCategoryId)
                    .collect(Collectors.toList());

            // 将父分类 ID 自己也加进去（这样父类下的商品和子类下的商品都能查出来）
            idList.add(categoryId);

            // 使用 IN 查询
            wrapper.in(Product::getCategoryId, idList);
        }

        if (sort != null && sort > 0) {
            // 用户主动选择了排序方式，禁止使用 RAND()
            switch (sort) {
                case 1 -> wrapper.orderByDesc(Product::getCreateTime);
                case 2 -> wrapper.orderByDesc(Product::getSalesActual);
                case 3 -> wrapper.orderByAsc(Product::getPrice);
                case 4 -> wrapper.orderByDesc(Product::getPrice);
            }
        } else {
            // 默认/综合排序逻辑
            if (StrUtil.isNotBlank(keyword)) {
                // 有搜索词：按销量排
                wrapper.orderByDesc(Product::getSalesActual);
            } else if (categoryId != null) {
                // 有分类没搜索词：按排序字段或销量
                wrapper.orderByAsc(Product::getSortOrder);
            } else {
                // 既没搜索、没分类、没排序：首页纯发现模式，使用随机
                wrapper.last("ORDER BY RAND()");
            }
        }

        wrapper.select(Product::getId, Product::getProductName, Product::getPrice,
                Product::getPic, Product::getIsNew, Product::getIsRecommend,
                Product::getSalesShow);

        IPage<Product> productPage = productService.page(page, wrapper);

        IPage<MallProductDTO> dtoPage = productPage.convert(this::convertToDTO);
        return Result.success(dtoPage);
    }

    // 获取商品详情
    @GetMapping("/detail/{id}")
    public Result<?> getProductDetail(@PathVariable Long id) {
        ProductSaveDTO product = productService.getProductDetail(id);

        if (product == null) {
            return Result.error("商品不存在或已下架", 404);
        }

        return Result.success(product);
    }

    @GetMapping("/sku/{skuId}")
    public Result<?> getProductDetailBySku(@PathVariable Long skuId){
        SkuStock skuStock = skuStockService.getById(skuId);
        if (skuStock == null) {
            return Result.error("商品不存在或已下架", 404);
        }

        return Result.success(skuStock);
    }

    /**
     * 转换工具方法：将 Product 转换为 MallProductDTO
     */
    private MallProductDTO convertToDTO(Product product) {
        MallProductDTO dto = new MallProductDTO();
        org.springframework.beans.BeanUtils.copyProperties(product, dto);
        return dto;
    }
}
