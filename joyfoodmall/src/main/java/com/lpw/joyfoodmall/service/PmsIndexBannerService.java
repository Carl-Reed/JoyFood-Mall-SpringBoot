package com.lpw.joyfoodmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lpw.joyfoodmall.entity.PmsIndexBanner;

import java.util.List;

public interface PmsIndexBannerService extends IService<PmsIndexBanner> {
    List<PmsIndexBanner> getVisibleBanners();

    void saveBanner(PmsIndexBanner banner);
}
