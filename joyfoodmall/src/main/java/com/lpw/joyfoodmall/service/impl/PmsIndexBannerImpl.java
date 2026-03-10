package com.lpw.joyfoodmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpw.joyfoodmall.entity.PmsIndexBanner;
import com.lpw.joyfoodmall.entity.SysFile;
import com.lpw.joyfoodmall.mapper.PmsIndexBannerMapper;
import com.lpw.joyfoodmall.service.PmsIndexBannerService;
import com.lpw.joyfoodmall.service.SysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PmsIndexBannerImpl extends ServiceImpl<PmsIndexBannerMapper, PmsIndexBanner> implements PmsIndexBannerService {

    private final SysFileService sysFileService;

    @Override
    public List<PmsIndexBanner> getVisibleBanners() {
        return this.list(new LambdaQueryWrapper<PmsIndexBanner>()
                .eq(PmsIndexBanner::getStatus, 1)
                .orderByDesc(PmsIndexBanner::getSort)); // 按排序字段倒序
    }

    @Override
    public void saveBanner(PmsIndexBanner banner){
        String oldPath = "";
        if(banner.getId()!=null){
            PmsIndexBanner oldBanner = this.getById(banner.getId());
            oldPath = oldBanner.getPic();
        }
        handleImageLifecycle(oldPath,banner.getPic());
        this.saveOrUpdate(banner);
    }

    private void handleImageLifecycle(String oldPath,String newPath){
        if (!oldPath.isEmpty()){
            sysFileService.update(new LambdaUpdateWrapper<SysFile>()
                    .in(SysFile::getFilePath, oldPath)
                    .set(SysFile::getIsUsed, 0));
        }
        if (!newPath.isEmpty()){
            sysFileService.update(new LambdaUpdateWrapper<SysFile>()
                    .in(SysFile::getFilePath, newPath)
                    .set(SysFile::getIsUsed, 1));
        }

    }
}
