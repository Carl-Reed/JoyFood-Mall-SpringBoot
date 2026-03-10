package com.lpw.joyfoodmall.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.entity.DTO.PageParams;
import com.lpw.joyfoodmall.entity.DTO.ProductSaveDTO;
import com.lpw.joyfoodmall.entity.Product;
import com.lpw.joyfoodmall.entity.SkuStock;
import com.lpw.joyfoodmall.service.ProductService;
import com.lpw.joyfoodmall.service.SkuStockService;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("products")
public class ProductController {

    private final ProductService productService;
    private final SkuStockService skuStockService;

    // 分页查询商品列表
    @GetMapping("list")
    public Result<?> list(PageParams params,
                          @RequestParam(required = false) String productName,
                          @RequestParam(required = false) Long categoryId) {
        // 创建分页对象
        Page<Product> page = new Page<>(params.getPage(), params.getLimit());

        // 构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 模糊查询名称
        if (StringUtils.hasText(productName)) {
            wrapper.like(Product::getProductName, productName);
        }
        // 分类查询
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        IPage<Product> result = productService.getProductListWithStock(page, wrapper);

        return Result.success(result);
    }

    // 新增商品
    @PostMapping
    public Result<?> create(@RequestBody ProductSaveDTO productDTO) {
        productService.saveProduct(productDTO);
        return Result.message("新增成功");
    }

    // 修改商品
    @PutMapping
    public Result<?> update(@RequestBody ProductSaveDTO productDTO) {
        // 包含id，方法执行更新操作
        productService.saveProduct(productDTO);
        return Result.message("更新成功");
    }

    // 获取单个商品详情（含 SKU）
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    // 更新商品状态
    @PatchMapping("/{id}/switch/{field}/{value}")
    public Result<?> updateProductStatus(
            @PathVariable Long id,
            @PathVariable String field,
            @PathVariable Integer value) {

        // 安全白名单：只允许修改这三个字段
        List<String> allowedFields = Arrays.asList("isPublish", "isNew", "isRecommend");
        if (!allowedFields.contains(field)) {
            return Result.error("非法操作：该字段不允许被快捷修改");
        }

        // 构造更新条件
        UpdateWrapper<Product> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);

        // 将驼峰 field 转换为数据库下划线字段名，例如 isPublish -> is_publish
        String columnName = StrUtil.toUnderlineCase(field);
        updateWrapper.set(columnName, value);

        boolean success = productService.update(updateWrapper);

        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    // 删除单个商品
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        productService.removeByIdWithSKU(id);
        return Result.message("删除成功");
    }

    // 批量删除商品
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestParam("ids") List<Long> ids) {
        // 删除 SPU 及其关联的 SKU
        productService.deleteBatchProducts(ids);
        return Result.message("批量删除成功");
    }

    // 根据SPU商品ID查询相关的SKU数据
    @GetMapping("/listSkuBySpu/{spuId}")
    public Result<List<SkuStock>> listBySpu(@PathVariable Long spuId) {
        List<SkuStock> skus = skuStockService.list(new LambdaQueryWrapper<SkuStock>()
                .eq(SkuStock::getProductId, spuId));
        return Result.success(skus);
    }

    @PatchMapping("/sku/publish")
    public Result<?> updateSKUPublish(@RequestBody SkuStock skuStock) {
        skuStockService.updateById(skuStock);
        return Result.message("状态更新成功");
    }
}