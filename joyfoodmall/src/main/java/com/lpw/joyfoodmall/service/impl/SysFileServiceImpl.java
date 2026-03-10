package com.lpw.joyfoodmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpw.joyfoodmall.entity.SysFile;
import com.lpw.joyfoodmall.mapper.SysFileMapper;
import com.lpw.joyfoodmall.service.SysFileService;
import com.lpw.joyfoodmall.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService {

    private final FileUtils fileUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUnusedFile(Long id) {
        // 严格条件查询
        SysFile file = this.getOne(new LambdaQueryWrapper<SysFile>()
                .eq(SysFile::getId, id)
                .eq(SysFile::getIsUsed, 0)
                .lt(SysFile::getCreateTime, LocalDateTime.now().minusHours(24)));

        if (file == null) return false;

        // 执行物理删除
        fileUtils.deletePhysicalFile(file.getFilePath());

        // 删除数据库记录
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> clearAllUnusedFiles() {
        // 筛选 24 小时前且未使用的文件
        List<SysFile> list = this.list(new LambdaQueryWrapper<SysFile>()
                .eq(SysFile::getIsUsed, 0)
                .lt(SysFile::getCreateTime, LocalDateTime.now().minusHours(24)));

        int count = 0;
        int failCount = 0;

        for (SysFile file : list) {
            // 物理删除
            if (fileUtils.deletePhysicalFile(file.getFilePath())) {
                this.removeById(file.getId());
                count++;
            }else {
                failCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("failCount", failCount);
        System.out.println(count);
        System.out.println(failCount);
        return result;
    }
}
