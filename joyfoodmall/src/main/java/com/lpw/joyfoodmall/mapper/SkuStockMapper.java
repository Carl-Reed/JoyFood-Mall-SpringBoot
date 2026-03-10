package com.lpw.joyfoodmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lpw.joyfoodmall.entity.SkuStock;
import org.apache.ibatis.annotations.Update;
import org.springframework.data.repository.query.Param;

public interface SkuStockMapper extends BaseMapper<SkuStock> {

    /**
     * 原子增加库存
     * SQL: UPDATE pms_sku_stock SET stock = stock + #{quantity} WHERE id = #{skuId}
     */
    @Update("UPDATE pms_sku_stock SET stock = stock + #{quantity} WHERE id = #{skuId}")
    int rollbackStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
