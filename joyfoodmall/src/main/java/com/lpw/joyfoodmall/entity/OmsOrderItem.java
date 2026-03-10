package com.lpw.joyfoodmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("oms_order_item")
public class OmsOrderItem extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 订单id */
    private Long orderId;

    /** 订单编号 */
    private String orderSn;

    /** 商品SPU ID */
    private Long productId;

    /** 商品SKU ID */
    private Long productSkuId;

    /** 商品名称 */
    private String productName;

    /** 商品图片 */
    private String productPic;

    /** 下单时价格 */
    private BigDecimal productPrice;

    /** 购买数量 */
    private Integer productQuantity;

    /** 商品销售属性:[{"key":"颜色","value":"黑色"}] */
    private String productAttr;
}