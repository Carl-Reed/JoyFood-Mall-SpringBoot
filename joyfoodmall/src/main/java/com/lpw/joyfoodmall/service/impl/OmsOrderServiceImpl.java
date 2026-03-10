package com.lpw.joyfoodmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements OmsOrderService {

    private final OmsOrderItemMapper orderItemMapper;
    private final SkuStockMapper skuMapper;
    private final OmsCartService cartService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, OrderParamDTO orderParam) {
        // 生成订单编号 (秒级时间戳 + 随机数)
        String orderSn = generateOrderSn();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OmsOrderItem> orderItems = new ArrayList<>();
        List<Long> skuIdsToRemove = new ArrayList<>();

        // 循环处理商品：校验库存、计算价格、封装详情
        for (OrderItemDTO itemDto : orderParam.getItemList()) {
            SkuStock sku = skuMapper.selectById(itemDto.getProductSkuId());
            if (sku == null || sku.getStock() < itemDto.getQuantity()) {
                throw new RuntimeException("商品库存不足：" + (sku != null ? sku.getSkuName() : "未知"));
            }

            // 扣减库存
            sku.setStock(sku.getStock() - itemDto.getQuantity());
            skuMapper.updateById(sku);

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

        // --- 费用设置 ---
        order.setTotalAmount(totalAmount);
        order.setFreightAmount(orderParam.getFreightAmount());
        // 应付金额 = 商品总额 + 运费
        order.setPayAmount(totalAmount.add(orderParam.getFreightAmount()));

        // --- 状态与属性 ---
        order.setStatus(0); // 待支付
        order.setPayType(0); // 未支付
        order.setNote(orderParam.getNote()); // 记录备注
        order.setCreateTime(LocalDateTime.now());

        // --- 填充收货地址快照 ---
        order.setMemberReceiveAddressId(orderParam.getMemberReceiveAddressId());
        order.setReceiverName(orderParam.getReceiverName());
        order.setReceiverPhone(orderParam.getReceiverPhone());
        order.setReceiverProvince(orderParam.getReceiverProvince());
        order.setReceiverCity(orderParam.getReceiverCity());
        order.setReceiverRegion(orderParam.getReceiverRegion());
        order.setReceiverDetailAddress(orderParam.getReceiverDetailAddress());

        this.save(order);

        // 批量保存订单详情项
        for (OmsOrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // 清理购物车
        if (!skuIdsToRemove.isEmpty()) {
            cartService.remove(new LambdaQueryWrapper<OmsCartItem>()
                    .eq(OmsCartItem::getUserId, userId)
                    .in(OmsCartItem::getProductSkuId, skuIdsToRemove));
        }

        return orderSn;
    }

    /** 生成订单号：yyyyMMdd + 6位随机数 */
    private String generateOrderSn() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        return date + random;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        // 加上行锁查询订单，防止并发取消冲突
        OmsOrder order = this.getOne(new LambdaQueryWrapper<OmsOrder>()
                .eq(OmsOrder::getId, orderId)
                .eq(OmsOrder::getUserId, currentUserId)
                .last("FOR UPDATE"));

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 只有待支付(0)的订单能取消
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单状态无法取消（可能已支付或已关闭）");
        }

        // 更新订单状态为已关闭(4)
        order.setStatus(4);
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);

        // 获取订单下的所有商品项
        List<OmsOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId)
        );

        // 回滚库存
        if (items != null && !items.isEmpty()) {
            for (OmsOrderItem item : items) {
                int count = skuMapper.rollbackStock(item.getProductSkuId(), item.getProductQuantity());
                if (count == 0) {
                    throw new RuntimeException("回滚商品 [" + item.getProductName() + "] 库存失败");
                }
            }
        }
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

    // 获取订单状态统计数据
    @Override
    public OmsOrderStatsDTO getOrderStats(Long userId) {
        return baseMapper.getOrderStats(userId);
    }
}
