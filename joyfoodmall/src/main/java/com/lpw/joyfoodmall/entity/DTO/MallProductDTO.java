package com.lpw.joyfoodmall.entity.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MallProductDTO {
    private Long id;
    private String productName;
    private BigDecimal price;
    private String pic;
    private Integer isPublish;
    private Integer isNew;
    private Integer isRecommend;
    private Integer salesShow;
}
