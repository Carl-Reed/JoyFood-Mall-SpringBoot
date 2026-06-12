package com.lpw.joyfoodmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lpw.joyfoodmall.entity.DTO.OmsOrderStatsDTO;
import com.lpw.joyfoodmall.entity.DTO.OrderParamDTO;
import com.lpw.joyfoodmall.entity.OmsOrder;

public interface OmsOrderService extends IService<OmsOrder> {
    /** 创建订单，原子锁定库存，生成待支付订单 */
    String createOrder(Long userId, OrderParamDTO orderParam);

    /** 取消订单（验证用户身份） */
    boolean cancelOrder(Long orderId);

    /** 取消订单（MQ消息到期自动调用） */
    boolean cancelOrderInternal(Long orderId);

    /** 删除订单 */
    void deleteOrder(Long orderId);

    /** 获取订单状态统计数据 */
    OmsOrderStatsDTO getOrderStats(Long userId);

    /** 订单完成支付回调处理，更新状态，扣减真实库存 */
    void paySuccess(Long orderId, Integer payType);
}
