package com.lpw.joyfoodmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_index_banner")
public class PmsIndexBanner extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String pic;   // 图片地址
    private String url;   // 点击图片跳转的地址
    private Integer sort; // 数字越大越靠前
    private Integer status;
    @TableLogic
    private Integer isDeleted;
}
