package com.lpw.joyfoodmall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.entity.OmsCartItem;
import com.lpw.joyfoodmall.entity.VO.CartUserStatisticsVO;
import com.lpw.joyfoodmall.mapper.OmsCartItemMapper;
import com.lpw.joyfoodmall.service.OmsCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/cart")
@RequiredArgsConstructor
public class OmsCartController {
    private final OmsCartService cartService;
    private final OmsCartItemMapper omsCartItemMapper;

    /** 获取有购物车的用户分页列表 */
    @GetMapping("/user/page")
    public Result<IPage<CartUserStatisticsVO>> getCartUserPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            Long userId,String username) {
        Page<CartUserStatisticsVO> pageParam = new Page<>(page, limit);
        return Result.success(omsCartItemMapper.getCartUserPage(pageParam, userId, username));
    }

    /** 获取指定用户的购物车详情 */
    @GetMapping("/list/{userId}")
    public Result<List<OmsCartItem>> getListByUserId(@PathVariable Long userId) {
        return Result.success(cartService.list(
                new LambdaQueryWrapper<OmsCartItem>().eq(OmsCartItem::getUserId, userId)
        ));
    }

    /** 管理员移除某项购物车商品 */
    @PreAuthorize("hasRole('ROOT')")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        cartService.removeById(id);
        return Result.message("移除成功");
    }

    /** 一键清空指定用户的购物车 */
    @PreAuthorize("hasRole('ROOT')")
    @DeleteMapping("/clear/{userId}")
    public Result<String> clearCart(@PathVariable Long userId) {
        cartService.remove(new LambdaQueryWrapper<OmsCartItem>()
                .eq(OmsCartItem::getUserId, userId));
        return Result.message("该用户购物车已清空");
    }
}
