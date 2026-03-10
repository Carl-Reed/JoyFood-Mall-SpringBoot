package com.lpw.joyfoodmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("oms_order")
public class OmsOrder extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 订单编号 */
    private String orderSn;
    /** 用户id */
    private Long userId;
    /** 订单总金额 */
    private BigDecimal totalAmount;
    /** 应付金额（实际支付） */
    private BigDecimal payAmount;
    /** 运费金额 */
    private BigDecimal freightAmount;
    /** 支付方式：0->未支付；1->支付宝；2->微信 */
    private Integer payType;
    /** 订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单 */
    private Integer status;
    /** 关联的收货地址ID */
    private Long memberReceiveAddressId;
    private String receiverName;
    private String receiverPhone;
    /** 省份/直辖市 */
    private String receiverProvince;
    /** 城市 */
    private String receiverCity;
    /** 区/县 */
    private String receiverRegion;
    /** 详细地址（街道门牌号） */
    private String receiverDetailAddress;
    /** 物流公司名称 */
    private String deliveryCompany;
    /** 物流单号 */
    private String deliverySn;
    /** 订单备注 */
    private String note;
    /** 确认收货状态：0->未确认；1->已确认 */
    private Integer confirmStatus;
    private LocalDateTime paymentTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;

    /** 订单所包含的商品列表 */
    @TableField(exist = false)
    List<OmsOrderItem> itemList;

    /** 逻辑删除标记：0->未删除；1->已删除 */
    @TableLogic
    private Integer isDeleted;
}