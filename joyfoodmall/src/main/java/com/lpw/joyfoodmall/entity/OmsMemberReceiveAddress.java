package com.lpw.joyfoodmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("oms_member_receive_address")
public class OmsMemberReceiveAddress {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String name;
    private String phoneNumber;
    private Integer defaultStatus; // 0非默认，1默认
    private String province;
    private String city;
    private String region;
    private String detailAddress;
}
