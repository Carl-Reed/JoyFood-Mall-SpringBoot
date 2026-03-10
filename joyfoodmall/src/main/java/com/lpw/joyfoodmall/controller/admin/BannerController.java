package com.lpw.joyfoodmall.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.entity.PmsIndexBanner;
import com.lpw.joyfoodmall.service.PmsIndexBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/banner")
public class BannerController {

    private final PmsIndexBannerService bannerService;

    /** 分页查询 Banner 列表 */
    @GetMapping("/page")
    public Result<IPage<PmsIndexBanner>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            String title) {
        Page<PmsIndexBanner> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<PmsIndexBanner> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotEmpty(title)) {
            queryWrapper.like(PmsIndexBanner::getTitle, title);
        }
        queryWrapper.orderByDesc(PmsIndexBanner::getSort);
        return Result.success(bannerService.page(pageParam, queryWrapper));
    }

    /** 添加 Banner */
    @PostMapping("/add")
    public Result<String> add(@RequestBody PmsIndexBanner banner) {
        bannerService.saveBanner(banner);
        return Result.success("添加成功");
    }

    /** 更新 Banner */
    @PutMapping("/update")
    public Result<String> update(@RequestBody PmsIndexBanner banner) {
        if (banner.getId() == null) {
            return Result.error("ID不能为空");
        }
        bannerService.saveBanner(banner);
        return Result.success("更新成功");
    }

    /** 删除 Banner */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        bannerService.removeById(id);
        return Result.success("删除成功");
    }

    /** 改变 Banner 状态 (上下架) */
    @PutMapping("/status/{id}/{status}")
    public Result<String> changeStatus(@PathVariable Long id, @PathVariable Integer status) {
        PmsIndexBanner banner = new PmsIndexBanner();
        banner.setId(id);
        banner.setStatus(status);
        bannerService.updateById(banner);
        return Result.success("状态更新成功");
    }
}
