package com.lpw.joyfoodmall.entity.DTO;

import lombok.Data;

@Data
public class PageParams {
    private Integer page = 1;  // 当前页，默认第1页
    private Integer limit = 10; // 每页显示条数，默认10条
}