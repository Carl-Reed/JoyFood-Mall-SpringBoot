package com.lpw.joyfoodmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("oms_cart_item")
public class OmsCartItem extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    private Long productSkuId;
    private Integer quantity;
    private BigDecimal price;
    private String productPic;
    private String productName;
    private String productAttr; // 存储如 [{"key":"颜色","value":"黑色"}]
}