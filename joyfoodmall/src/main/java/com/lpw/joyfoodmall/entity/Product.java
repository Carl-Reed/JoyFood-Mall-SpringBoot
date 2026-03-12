package com.lpw.joyfoodmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("pms_product")
public class Product extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String productName;
    private BigDecimal price;
    private String pic;
    private String albumPics;
    private String detailHtml;
    private String unit;
    private Integer isPublish;
    private Integer isNew;
    private Integer isRecommend;
    private Integer salesActual;
    private Integer salesShow;
    @TableLogic // 标记为逻辑删除字段
    private Integer isDeleted;

    // 最低价格
    @TableField(exist = false)
    private BigDecimal minPrice;

    // 最高价格
    @TableField(exist = false)
    private BigDecimal maxPrice;

    // 总库存
    @TableField(exist = false)
    private Integer totalStock;
}
