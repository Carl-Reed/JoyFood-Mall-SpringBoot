package com.lpw.joyfoodmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpw.joyfoodmall.entity.OmsCartItem;
import com.lpw.joyfoodmall.mapper.OmsCartItemMapper;
import com.lpw.joyfoodmall.service.OmsCartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OmsCartServiceImpl extends ServiceImpl<OmsCartItemMapper, OmsCartItem> implements OmsCartService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCart(OmsCartItem cartItem) {
        // 查询该用户的购物车中是否已有该规格商品 (根据 userId 和 productSkuId)
        OmsCartItem existItem = this.getOne(new LambdaQueryWrapper<OmsCartItem>()
                .eq(OmsCartItem::getUserId, cartItem.getUserId())
                .eq(OmsCartItem::getProductSkuId, cartItem.getProductSkuId())
                .last("LIMIT 1") // 优化查询性能
        );

        if (existItem == null) {
            // 购物车中没有此商品，直接新增
            this.save(cartItem);
        } else {
            // 购物车已有此商品，累加数量
            // 累加前端传来的数量
            int newQuantity = existItem.getQuantity() + cartItem.getQuantity();
            existItem.setQuantity(newQuantity);

            // 更新价格
            existItem.setPrice(cartItem.getPrice());

            existItem.setUpdateTime(LocalDateTime.now());

            // 执行更新操作
            this.updateById(existItem);
        }
    }
}
