package com.lpw.joyfoodmall.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpw.joyfoodmall.component.rabbitmq.OrderProducer;
import com.lpw.joyfoodmall.entity.DTO.OmsOrderStatsDTO;
import com.lpw.joyfoodmall.entity.DTO.OrderItemDTO;
import com.lpw.joyfoodmall.entity.DTO.OrderParamDTO;
import com.lpw.joyfoodmall.entity.OmsCartItem;
import com.lpw.joyfoodmall.entity.OmsOrder;
import com.lpw.joyfoodmall.entity.OmsOrderItem;
import com.lpw.joyfoodmall.entity.SkuStock;
import com.lpw.joyfoodmall.mapper.OmsOrderItemMapper;
import com.lpw.joyfoodmall.mapper.OmsOrderMapper;
import com.lpw.joyfoodmall.mapper.SkuStockMapper;
import com.lpw.joyfoodmall.service.OmsCartService;
import com.lpw.joyfoodmall.service.OmsOrderService;
import com.lpw.joyfoodmall.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements OmsOrderService {

    private final OmsOrderItemMapper orderItemMapper;
    private final SkuStockMapper skuMapper;
    private final OmsCartService cartService;
    private final Snowflake snowflake;
    private final OrderProducer orderProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, OrderParamDTO orderParam) {
        // 使用雪花算法生成订单编号
        String orderSn = String.valueOf(snowflake.nextId());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OmsOrderItem> orderItems = new ArrayList<>();
        List<Long> skuIdsToRemove = new ArrayList<>();

        List<Long> skuIds = orderParam.getItemList().stream()
                .map(OrderItemDTO::getProductSkuId).collect(Collectors.toList());

        Map<Long, SkuStock> skuMap = skuMapper.selectByIds(skuIds).stream()
                .collect(Collectors.toMap(SkuStock::getId, Function.identity()));

        // 循环处理商品：校验库存、计算价格、封装详情
        for (OrderItemDTO itemDto : orderParam.getItemList()) {
            // 通过数据库行锁进行库存原子扣减锁定
            int count = skuMapper.lockStock(itemDto.getProductSkuId(), itemDto.getQuantity());
            if (count == 0) {
                SkuStock sku = skuMap.get(itemDto.getProductSkuId());
                String name = sku != null ? sku.getSkuName() : "未知商品";
                throw new RuntimeException("商品 [" + name + "] 库存不足，无法下单");
            }

            // 锁定成功后，查询商品信息用于组装订单明细
            SkuStock sku = skuMap.get(itemDto.getProductSkuId());
            // 累计商品总额
            BigDecimal itemAmount = sku.getPrice().multiply(new BigDecimal(itemDto.getQuantity()));
            totalAmount = totalAmount.add(itemAmount);

            // 封装详情记录
            OmsOrderItem orderItem = new OmsOrderItem();
            orderItem.setOrderSn(orderSn);
            orderItem.setProductId(sku.getProductId());
            orderItem.setProductSkuId(sku.getId());
            orderItem.setProductName(sku.getSkuName());
            orderItem.setProductPrice(sku.getPrice());
            orderItem.setProductQuantity(itemDto.getQuantity());
            orderItem.setProductPic(sku.getPic());
            orderItem.setProductAttr(sku.getSpData());
            orderItems.add(orderItem);

            skuIdsToRemove.add(itemDto.getProductSkuId());
        }

        // 保存订单主表
        OmsOrder order = new OmsOrder();
        order.setOrderSn(orderSn);
        order.setUserId(userId);

        // 费用设置
        order.setTotalAmount(totalAmount);
        order.setFreightAmount(orderParam.getFreightAmount());
        // 应付金额 = 商品总额 + 运费
        order.setPayAmount(totalAmount.add(orderParam.getFreightAmount()));

        // 状态与属性
        order.setStatus(0); // 待支付
        order.setPayType(0); // 未支付
        order.setNote(orderParam.getNote()); // 记录备注
        order.setCreateTime(LocalDateTime.now());

        // 填充收货地址快照
        order.setMemberReceiveAddressId(orderParam.getMemberReceiveAddressId());
        order.setReceiverName(orderParam.getReceiverName());
        order.setReceiverPhone(orderParam.getReceiverPhone());
        order.setReceiverProvince(orderParam.getReceiverProvince());
        order.setReceiverCity(orderParam.getReceiverCity());
        order.setReceiverRegion(orderParam.getReceiverRegion());
        order.setReceiverDetailAddress(orderParam.getReceiverDetailAddress());

        this.save(order);

        // 注册事务同步回调，确保只有在事务成功提交后，才发送MQ消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderProducer.sendOrderCancelDelayMsg(order.getId());
            }
        });

        // 批量保存订单详情项
        for (OmsOrderItem item : orderItems) {
            item.setOrderId(order.getId());
        }
        orderItems.forEach(orderItemMapper::insert);

        // 清理购物车
        if (!skuIdsToRemove.isEmpty()) {
            cartService.remove(new LambdaQueryWrapper<OmsCartItem>()
                    .eq(OmsCartItem::getUserId, userId)
                    .in(OmsCartItem::getProductSkuId, skuIdsToRemove));
        }

        return orderSn;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        // 加上行锁查询订单，防止并发取消冲突，同时校验用户归属权
        OmsOrder order = this.getOne(new LambdaQueryWrapper<OmsOrder>()
                .eq(OmsOrder::getId, orderId)
                .eq(OmsOrder::getUserId, currentUserId)
                .last("FOR UPDATE"));

        if (order == null) {
            log.warn("订单不存在或无权操作");
            return false;
        }

        return doCancelOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrderInternal(Long orderId) {
        OmsOrder order = this.getOne(new LambdaQueryWrapper<OmsOrder>()
                .eq(OmsOrder::getId, orderId)
                .last("FOR UPDATE"));

        if (order == null || order.getStatus() == 4) {
            log.warn("MQ触发自动取消：订单不存在或已关闭，跳过处理。订单ID: {}", orderId);
            return false;
        }
        return doCancelOrder(order);
    }

    // 公共取消订单逻辑
    private boolean doCancelOrder(OmsOrder order) {
        // 只有待支付(0)的订单能取消
        if (order.getStatus() != 0) {
            log.info("MQ触发自动取消拦截：订单 {} 当前状态为 {} (可能已支付)，跳过取消",
                    order.getId(), order.getStatus());
            return false;
        }

        // 更新订单状态为已关闭(4)
        order.setStatus(4);
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);

        // 释放锁定的库存
        List<OmsOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, order.getId()));

        for (OmsOrderItem item : items) {
            int count = skuMapper.releaseStock(item.getProductSkuId(), item.getProductQuantity());
            if (count == 0) {
                log.warn("订单 {} 的商品 {} 库存释放失败，可能已释放过或不存在锁定记录",
                        order.getId(), item.getProductSkuId());
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long orderId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        // 查找订单
        OmsOrder order = this.getOne(new LambdaQueryWrapper<OmsOrder>()
                .eq(OmsOrder::getId, orderId)
                .eq(OmsOrder::getUserId,currentUserId));
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 只有“已完成(3)”或“已关闭(4)”的订单才允许被删除
        if (order.getStatus() < 3) {
            throw new RuntimeException("订单正在进行中，无法删除");
        }

        boolean success = this.remove(new LambdaUpdateWrapper<OmsOrder>()
                .eq(OmsOrder::getId, orderId)
                .eq(OmsOrder::getUserId, currentUserId));

        if (!success) {
            throw new RuntimeException("删除失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(Long orderId,Integer payType) {
        OmsOrder order = this.getById(orderId);
        if (order == null || order.getStatus() != 0) {
            log.warn("订单不存在或状态非待支付，跳过处理。订单ID: {}", orderId);
            return;
        }

        // 更新订单状态为待发货
        order.setStatus(1);
        order.setPayType(payType);
        order.setPaymentTime(LocalDateTime.now());
        this.updateById(order);

        // 扣减真实库存，释放锁定库存
        List<OmsOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));

        for (OmsOrderItem item : items) {
            int count = skuMapper.reduceSkuStock(item.getProductSkuId(), item.getProductQuantity());
            if (count == 0) {
                throw new RuntimeException("支付成功但扣减真实库存失败！订单ID：" + orderId);
            }
        }
    }

    // 获取订单状态统计数据
    @Override
    public OmsOrderStatsDTO getOrderStats(Long userId) {
        return baseMapper.getOrderStats(userId);
    }
}
