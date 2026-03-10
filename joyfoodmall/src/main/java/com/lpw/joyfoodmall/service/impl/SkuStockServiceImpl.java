package com.lpw.joyfoodmall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpw.joyfoodmall.entity.SkuStock;
import com.lpw.joyfoodmall.mapper.SkuStockMapper;
import com.lpw.joyfoodmall.service.SkuStockService;
import org.springframework.stereotype.Service;

@Service
public class SkuStockServiceImpl extends ServiceImpl<SkuStockMapper, SkuStock> implements SkuStockService{
}
