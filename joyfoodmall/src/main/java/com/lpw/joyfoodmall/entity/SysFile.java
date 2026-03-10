package com.lpw.joyfoodmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

// 文件上传记录表
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_file")
public class SysFile extends BaseEntity{
    @TableId(type = IdType.AUTO)
    private Long id;
    private String filePath;
    private String fileType;
    private Integer isUsed;
}
