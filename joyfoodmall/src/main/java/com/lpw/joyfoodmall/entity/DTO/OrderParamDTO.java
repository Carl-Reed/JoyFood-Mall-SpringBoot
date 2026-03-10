package com.lpw.joyfoodmall.entity.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderParamDTO {

    @NotNull(message = "收货地址ID不能为空")
    private Long memberReceiveAddressId; // 关联的地址簿ID

    @NotEmpty(message = "收货人姓名不能为空")
    private String receiverName;

    @NotEmpty(message = "收货人电话不能为空")
    private String receiverPhone;

    @NotEmpty(message = "省份不能为空")
    private String receiverProvince;

    @NotEmpty(message = "城市不能为空")
    private String receiverCity;

    @NotEmpty(message = "区/县不能为空")
    private String receiverRegion;

    @NotEmpty(message = "详细地址不能为空")
    private String receiverDetailAddress;

    private String note;

    @NotNull(message = "运费不能为空")
    private BigDecimal freightAmount = BigDecimal.ZERO;

    @NotEmpty(message = "订单商品列表不能为空")
    private List<OrderItemDTO> itemList;
}