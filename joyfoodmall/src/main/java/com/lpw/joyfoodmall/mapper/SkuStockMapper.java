package com.lpw.joyfoodmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lpw.joyfoodmall.entity.SkuStock;
import org.apache.ibatis.annotations.Update;
import org.springframework.data.repository.query.Param;

public interface SkuStockMapper extends BaseMapper<SkuStock> {

    /**下单时锁定库存 */
    @Update("""
            UPDATE pms_sku_stock
            SET lock_stock = lock_stock + #{quantity}
            WHERE id = #{skuId}
            AND (stock - lock_stock) >= #{quantity}""")
    int lockStock(@Param("skuId")Long skuId,@Param("quantity")int quantity);

    /** 支付成功时扣减真实库存，释放锁定库存 */
    @Update("""
            UPDATE pms_sku_stock
            SET stock = stock - #{quantity},
               lock_stock = lock_stock - #{quantity}
            WHERE id = #{skuId}
               AND stock >= #{quantity}
               AND lock_stock >= #{quantity}""")
    int reduceSkuStock(@Param("skuId")Long skuId,@Param("quantity")int quantity);

    /** 取消订单/超时未支付时，释放锁定库存 */
    @Update("""
            UPDATE pms_sku_stock
            SET lock_stock = lock_stock - #{quantity}
            WHERE id = #{skuId}
                AND lock_stock >= #{quantity}
            """)
    int releaseStock(@Param("skuId")Long skuId,@Param("quantity")int quantity);
}
