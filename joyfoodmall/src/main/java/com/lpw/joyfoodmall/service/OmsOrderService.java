package com.lpw.joyfoodmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lpw.joyfoodmall.entity.DTO.OmsOrderStatsDTO;
import com.lpw.joyfoodmall.entity.DTO.OrderParamDTO;
import com.lpw.joyfoodmall.entity.OmsOrder;

public interface OmsOrderService extends IService<OmsOrder> {
    /** 创建订单 */
    String createOrder(Long userId, OrderParamDTO orderParam);

    /** 取消订单 */
    void cancelOrder(Long orderId);

    /** 删除订单 */
    void deleteOrder(Long orderId);

    /** 获取订单状态统计数据 */
    OmsOrderStatsDTO getOrderStats(Long userId);
}
