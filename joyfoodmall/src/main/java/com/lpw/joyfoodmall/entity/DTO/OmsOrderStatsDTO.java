package com.lpw.joyfoodmall.entity.DTO;

import lombok.Data;

@Data
public class OmsOrderStatsDTO {
    private Integer status0; // 待付款
    private Integer status1; // 待发货
    private Integer status2; // 待收货
    private Integer status3; // 已完成/待评价
}
