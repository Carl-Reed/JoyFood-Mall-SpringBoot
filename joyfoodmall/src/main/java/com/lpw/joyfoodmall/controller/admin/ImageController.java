package com.lpw.joyfoodmall.controller.admin;

import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("image")
public class ImageController {
    private final ImageService imageService;


    // =============== 用户头像相关 ===============
    // 上传头像
    @PostMapping("avatar/upload")
    public Result<?> uploadAvatar(
            @RequestParam("id") Integer id,
            @RequestParam("file") MultipartFile file) {
        try {
            imageService.saveAvatar(id, file);
            return Result.message("头像修改成功");
        } catch (IOException e) {
            return Result.error("头像修改失败");
        }
    }

    // =============== 商品图片相关 ===============
    // 上传商品图片
    @PostMapping("product/upload")
    public Result<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            String productImageUrl = imageService.saveProductImage(file);
            return Result.success(productImageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败");
        }
    }

    // =============== 轮播图片相关 ===============
    // 上传轮播图
    @PostMapping("banner/upload")
    public Result<?> uploadBannerImage(@RequestParam("file") MultipartFile file) {
        try {
            String bannerImageUrl = imageService.saveBannerImage(file);
            return Result.success(bannerImageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败");
        }
    }

}
