package com.lpw.joyfoodmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lpw.joyfoodmall.entity.SysFile;

import java.util.Map;

public interface SysFileService extends IService<SysFile> {
    boolean deleteUnusedFile(Long id);
    Map<String, Object> clearAllUnusedFiles();
}
