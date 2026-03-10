package com.lpw.joyfoodmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("pms_sku_stock")
public class SkuStock extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String skuName;
    private String skuCode;
    private BigDecimal price;
    private BigDecimal promotionPrice;
    private Integer stock;
    private Integer lowStock;
    private String pic;
    private String spData; // 存储规格 JSON，如 [{"key":"颜色","value":"黑色"}]
    private Integer isPublish;

    @TableField(exist = false)
    private String specName;   // 用于接收前端传来的 "颜色:红;尺寸:M"

    @TableLogic // 标记为逻辑删除字段
    private Integer isDeleted;
}