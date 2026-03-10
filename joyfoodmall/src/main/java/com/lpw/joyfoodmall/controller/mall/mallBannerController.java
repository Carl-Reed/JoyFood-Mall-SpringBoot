package com.lpw.joyfoodmall.controller.mall;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.entity.PmsIndexBanner;
import com.lpw.joyfoodmall.service.PmsIndexBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mall/banner")
public class mallBannerController {
    private final PmsIndexBannerService bannerService;
    @GetMapping("/list")
    public Result<?> list(Integer limit){
        List<PmsIndexBanner> banners = bannerService.list(new LambdaUpdateWrapper<PmsIndexBanner>()
                .eq(PmsIndexBanner::getStatus, 1)        // 必须是已上架
                .orderByDesc(PmsIndexBanner::getSort)       // 排序值越大越靠前
                .last("LIMIT " + limit)              // 限制首页展示数量，防止加载过多
                );
        return Result.success(banners);
    }
}
