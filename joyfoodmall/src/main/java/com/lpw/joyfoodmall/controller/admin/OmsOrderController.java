package com.lpw.joyfoodmall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.entity.DTO.OmsOrderStatsDTO;
import com.lpw.joyfoodmall.entity.DTO.OrderParamDTO;
import com.lpw.joyfoodmall.entity.DTO.PageParams;
import com.lpw.joyfoodmall.entity.OmsOrder;
import com.lpw.joyfoodmall.entity.OmsOrderItem;
import com.lpw.joyfoodmall.mapper.OmsOrderMapper;
import com.lpw.joyfoodmall.service.OmsOrderItemService;
import com.lpw.joyfoodmall.service.OmsOrderService;
import com.lpw.joyfoodmall.service.ProductService;
import com.lpw.joyfoodmall.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OmsOrderController {

    private final OmsOrderService orderService;
    private final OmsOrderMapper orderMapper;
    private final OmsOrderItemService orderItemService;
    private final ProductService productService;

    /** 分页查询订单列表（主表） */
    @GetMapping("/list")
    public Result<Page<OmsOrder>> list(
            PageParams params,
            String orderSn,
            Integer status) {

        Page<OmsOrder> orderPage = new Page<>(params.getPage(), params.getLimit());
        LambdaQueryWrapper<OmsOrder> queryWrapper = new LambdaQueryWrapper<>();

        // 筛选条件
        queryWrapper.like(StringUtils.hasText(orderSn), OmsOrder::getOrderSn, orderSn)
                .eq(status != null, OmsOrder::getStatus, status)
                .orderByDesc(OmsOrder::getCreateTime);

        return Result.success(orderService.page(orderPage, queryWrapper));
    }

    /** 查询用户的订单 */
    @GetMapping("/list-user")
    public Result<?> listUser(PageParams params,Integer status){
        Page<OmsOrder> orderPage = new Page<>(params.getPage(), params.getLimit());
        return Result.success(orderMapper.getListWithItem(orderPage,SecurityUtils.getCurrentUserId(),status));
    }

    /** 根据订单ID获取商品明细（用于前端表格展开） */
    @GetMapping("/items/{orderId}")
    public Result<List<OmsOrderItem>> getItems(@PathVariable Long orderId) {
        List<OmsOrderItem> list = orderItemService.list(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId)
        );
        return Result.success(list);
    }

    /** 创建订单 */
    @PostMapping("/create")
    public Result<String> createOrder(@Valid @RequestBody OrderParamDTO orderParam) {
        Long userId = SecurityUtils.getCurrentUserId();
        String orderSn = orderService.createOrder(userId, orderParam);
        return Result.success(orderSn);
    }

    /** 取消订单 */
    @PostMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.message("订单已取消");
    }

    /** 删除订单 */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id){
        orderService.deleteOrder(id);
        return Result.message("订单已删除");
    }

    /** 支付后调整订单状态 */
    @PostMapping("/pay")
    public Result<String> pay(@RequestParam("orderId") Long id,
                              @RequestParam("payType") Integer payType){
        OmsOrder order = orderService.getById(id);

        if (order == null) {
            return Result.error("订单不存在");
        }

        // 只有status=0状态可以支付
        if (order.getStatus() != 0) {
            return Result.error("当前订单状态无法支付，无法操作");
        }

        orderService.paySuccess(id,payType);
        return Result.message("支付成功，商品待发货");
    }

    /** 获取订单状态统计数据 */
    @GetMapping("/stats")
    public Result<OmsOrderStatsDTO> getOrderStats() {
        Long userId = SecurityUtils.getCurrentUserId();
        OmsOrderStatsDTO stats = orderService.getOrderStats(userId);
        return Result.success(stats);
    }

    /** 订单发货 */
    @PostMapping("/delivery")
    public Result<Object> delivery(@RequestBody OmsOrder updateOrder) {
        OmsOrder order = orderService.getById(updateOrder.getId());

        if (order == null) {
            return Result.error("订单不存在");
        }

        // 只有待发货（status=1）状态可以发货
        if (order.getStatus() != 1) {
            return Result.error("当前订单状态不是待发货，无法操作");
        }

        order.setStatus(2); // 变更状态为：已发货
        order.setDeliverySn(updateOrder.getDeliverySn());
        order.setDeliveryCompany(updateOrder.getDeliveryCompany());
        order.setDeliveryTime(LocalDateTime.now());

        boolean success = orderService.updateById(order);

        if (success) {
            return Result.message("发货成功");
        } else {
            return Result.error("数据库更新失败");
        }
    }

    /** 确认收货 */
    @PostMapping("/confirm/{orderId}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> confirmReceipt(@PathVariable Long orderId) {
        OmsOrder order = orderService.getById(orderId);
        // 只有待收货(2)的订单可以确认收货
        if (order == null || order.getStatus() != 2) {
            return Result.error("订单状态错误，无法确认收货");
        }

        order.setStatus(3); // 状态变为已完成
        order.setReceiveTime(LocalDateTime.now()); // 记录收货时间
        orderService.updateById(order);

        // 同步增加商品实际销量
        productService.increaseProductSales(orderId);
        return Result.message("确认收货成功");
    }
}