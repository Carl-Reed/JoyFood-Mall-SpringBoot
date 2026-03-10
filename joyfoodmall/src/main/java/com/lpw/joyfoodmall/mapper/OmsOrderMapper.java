package com.lpw.joyfoodmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.entity.DTO.OmsOrderStatsDTO;
import com.lpw.joyfoodmall.entity.OmsOrder;
import org.springframework.data.repository.query.Param;

public interface OmsOrderMapper extends BaseMapper<OmsOrder> {
    // 获取用户订单信息及订单内容
    IPage<OmsOrder> getListWithItem(Page<OmsOrder> page, @Param("userId") Long userId, @Param("status") Integer status);

    // 获取订单状态统计数据
    OmsOrderStatsDTO getOrderStats(@Param("userId") Long userId);
}
