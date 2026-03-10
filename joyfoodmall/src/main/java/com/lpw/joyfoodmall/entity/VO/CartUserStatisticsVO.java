package com.lpw.joyfoodmall.entity.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartUserStatisticsVO {
    private Long userId;
    private String username; // 关联会员表获取
    private Integer itemCount; // 加购商品总数
    private BigDecimal totalPrice; // 购物车总金额
    private LocalDateTime lastUpdateTime;
}