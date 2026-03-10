package com.lpw.joyfoodmall.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileUtils {

    @Value("${file.upload-path}")
    private String uploadRelativePath; // uploads/

    @Value("${file.access-path}")
    private String accessPath; // /files/

    /** 获取物理存储的绝对根路径 */
    public String getAbsoluteRootPath() {
        return System.getProperty("user.dir") + File.separator + uploadRelativePath;
    }

    /** 提取文件后缀名 */
    public String extractFileExtension(String fileName) {
        if (fileName == null) return ".bin";
        int lastIndex = fileName.lastIndexOf(".");
        // 如果找到了点，就截取点及其后面的所有字符。若没有点，则返回默认后缀
        return (lastIndex >= 0) ? fileName.substring(lastIndex) : ".bin";
    }

    /**
     * 物理文件删除
     * @param url 数据库存的访问路径，如 /files/images/xxx.jpg
     */
    public boolean deletePhysicalFile(String url) {
        if (url == null || !url.startsWith(accessPath)) {
            return false;
        }
        try {
            // 去掉前缀并转换分隔符
            String relativePath = url.replace(accessPath, "").replace("/", File.separator);
            // 拼接绝对路径
            Path fullPath = Paths.get(getAbsoluteRootPath(), relativePath);
            // 物理删除
            return Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            return false;
        }
    }
}
