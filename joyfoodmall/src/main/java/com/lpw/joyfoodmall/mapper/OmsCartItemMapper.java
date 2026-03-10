package com.lpw.joyfoodmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.entity.OmsCartItem;
import com.lpw.joyfoodmall.entity.VO.CartUserStatisticsVO;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

public interface OmsCartItemMapper extends BaseMapper<OmsCartItem> {
    @Select("<script>" +
            "SELECT c.user_id, u.username, COUNT(c.id) as item_count, " +
            "SUM(c.price * c.quantity) as total_price, MAX(c.create_time) as last_update_time " +
            "FROM oms_cart_item c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "<where>" +
            "  <if test='userId != null'>AND c.user_id = #{userId}</if>" +
            "  <if test='username != null and username != \"\"'>AND u.username LIKE CONCAT('%',#{username},'%')</if>" +
            "</where>" +
            "GROUP BY c.user_id, u.username" +
            "</script>")
    IPage<CartUserStatisticsVO> getCartUserPage(Page<?> page, @Param("userId") Long userId, @Param("username") String username);
}
