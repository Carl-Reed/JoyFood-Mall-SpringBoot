package com.lpw.joyfoodmall.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String executeUpload (String folder, MultipartFile file);
}
