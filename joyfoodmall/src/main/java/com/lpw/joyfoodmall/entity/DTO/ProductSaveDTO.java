package com.lpw.joyfoodmall.entity.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductSaveDTO {
    private Long id; // 编辑时使用
    private String productName;
    private Long categoryId;
    private Integer salesShow;
    private String pic;
    private String albumPics; // 前端传来的 JSON 字符串
    private String unit;
    private String description;
    private Integer isPublish;

    private List<SkuStockDTO> skuList;

    @Data
    public static class SkuStockDTO {
        private Long id;
        private String skuCode;
        private BigDecimal price;
        private BigDecimal promotionPrice;
        private Integer stock;
        private Integer lockStock;
        private String specName; // 格式如 "颜色:红;尺寸:M"
        private String pic;
        private String spData;
    }
}
