package com.lpw.joyfoodmall.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService{
    void saveAvatar(Integer userId, MultipartFile file) throws IOException;
    String saveProductImage(MultipartFile file);
    String saveBannerImage(MultipartFile file);

}
