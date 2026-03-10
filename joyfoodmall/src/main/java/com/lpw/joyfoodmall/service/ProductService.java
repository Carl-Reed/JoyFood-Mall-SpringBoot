package com.lpw.joyfoodmall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lpw.joyfoodmall.entity.DTO.ProductSaveDTO;
import com.lpw.joyfoodmall.entity.Product;

import java.util.List;

public interface ProductService extends IService<Product> {
    IPage<Product> getProductListWithStock(Page<Product> page, LambdaQueryWrapper<Product> wrapper);
    void saveProduct(ProductSaveDTO saveDTO);
    void removeByIdWithSKU(Long id);
    void deleteBatchProducts(List<Long> ids);
    ProductSaveDTO getProductDetail(Long id);
    List<Product> getNewArrivals(int limit);
    void increaseProductSales(Long orderId);
}
