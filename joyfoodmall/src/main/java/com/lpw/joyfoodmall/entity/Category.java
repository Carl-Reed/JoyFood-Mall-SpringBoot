package com.lpw.joyfoodmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pms_product_category")
public class Category extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;

    private Long parentId;

    private String categoryName;

    private String description;

    private Integer sortOrder;

    private Integer isEnable;

    @TableLogic // 标记为逻辑删除字段
    private Integer isDeleted;


}
