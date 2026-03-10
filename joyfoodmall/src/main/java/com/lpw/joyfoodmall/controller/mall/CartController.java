package com.lpw.joyfoodmall.controller.mall;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.entity.OmsCartItem;
import com.lpw.joyfoodmall.service.OmsCartService;
import com.lpw.joyfoodmall.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mall/cart")
public class CartController {

    private final OmsCartService cartService;

    // 获取当前用户购物车列表
    @GetMapping("/list")
    public Result<List<OmsCartItem>> list(){
        LambdaQueryWrapper<OmsCartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsCartItem::getUserId,SecurityUtils.getCurrentUserId());
        return Result.success(cartService.list(wrapper));
    }

    // 添加/修改商品到购物车
    @PostMapping("/add")
    public Result<?> add(@RequestBody OmsCartItem cartItem){
        cartItem.setUserId(SecurityUtils.getCurrentUserId());
        cartService.addCart(cartItem);
        return Result.message("添加/修改购物车成功！");
    }

    // 删除购物车项
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id){
        cartService.removeById(id);
        return Result.message("删除购物车项成功！");
    }
}
