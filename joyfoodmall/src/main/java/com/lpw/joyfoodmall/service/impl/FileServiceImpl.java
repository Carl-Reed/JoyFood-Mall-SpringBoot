package com.lpw.joyfoodmall.service.impl;

import com.lpw.joyfoodmall.entity.SysFile;
import com.lpw.joyfoodmall.service.FileService;
import com.lpw.joyfoodmall.service.SysFileService;
import com.lpw.joyfoodmall.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final SysFileService sysFileService;
    private final FileUtils fileUtils;

    @Value("${file.access-path}")
    private String accessPath; // /files/

    @Override
    public String executeUpload (String folder, MultipartFile file){
        if (file.isEmpty()) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "上传的文件为空");
        }

        String newFileName = UUID.randomUUID().toString().replace("-", "")
                + fileUtils.extractFileExtension(file.getOriginalFilename());

        // 物理存储路径 (uploads/folder/xxx)
        String subPath = folder + "/" + newFileName;
        String fullPath = fileUtils.getAbsoluteRootPath() + folder + File.separator;

        File destDir = new File(fullPath);
        // 若不存在则创建
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try {
            file.transferTo(new File(destDir, newFileName));
            // 访问路径：/files/folder/xxx
            String visitPath = accessPath + subPath;

            SysFile sysFile = new SysFile();
            sysFile.setFilePath(visitPath); // 存入数据库的是完整的访问路径，方便后续对比
            sysFile.setIsUsed(0);           // 初始状态为未使用
            sysFile.setFileType(fileUtils.extractFileExtension(visitPath));
            sysFileService.save(sysFile);

            // 返回统一访问路径：/files/folder/newFileName
            return accessPath + folder + "/" + newFileName;
        } catch (IOException e) {
            throw new RuntimeException("上传失败: " + e.getMessage());
        }
    }
}
