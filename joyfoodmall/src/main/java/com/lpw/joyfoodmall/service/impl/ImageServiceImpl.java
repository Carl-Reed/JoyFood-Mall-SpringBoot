package com.lpw.joyfoodmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lpw.joyfoodmall.entity.SysFile;
import com.lpw.joyfoodmall.entity.User;
import com.lpw.joyfoodmall.service.FileService;
import com.lpw.joyfoodmall.service.ImageService;
import com.lpw.joyfoodmall.service.SysFileService;
import com.lpw.joyfoodmall.service.UserService;
import com.lpw.joyfoodmall.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final UserService userService;
    private final FileService fileService;
    private final FileUtils fileUtils;
    private final SysFileService sysFileService;

    /** 保存用户头像 */
    @Transactional
    @Override
    public void saveAvatar(Integer userId, MultipartFile file) {
        User user = userService.getById(userId);
        String oldAvatarUrl = user.getAvatar();

        // 上传到 "avatars" 文件夹
        String newUrl = fileService.executeUpload("images/avatars", file);

        // 文件上传记录表设置为已使用
        sysFileService.update(new LambdaUpdateWrapper<SysFile>()
                .eq(SysFile::getFilePath, newUrl)
                .set(SysFile::getIsUsed, 1));

        // 更新数据库
        user.setAvatar(newUrl);
        userService.updateById(user);

        // 如果更新成功，尝试删除旧物理文件
        if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
            // 删除对应的头像上传记录
            sysFileService.remove(new LambdaUpdateWrapper<SysFile>()
                    .eq(SysFile::getFilePath,oldAvatarUrl));
            fileUtils.deletePhysicalFile(oldAvatarUrl);
        }
    }

    /** 保存商品图 */
    @Transactional
    @Override
    public String saveProductImage(MultipartFile file) {
        // 上传到 "productImages" 文件夹，返回图片链接
        return fileService.executeUpload("images/productImages", file);
    }

    /** 保存轮播图 */
    @Transactional
    @Override
    public String saveBannerImage(MultipartFile file){
        return fileService.executeUpload("images/bannerImages",file);
    }
}
