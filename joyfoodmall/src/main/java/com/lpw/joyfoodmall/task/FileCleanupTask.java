package com.lpw.joyfoodmall.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lpw.joyfoodmall.entity.SysFile;
import com.lpw.joyfoodmall.service.SysFileService;
import com.lpw.joyfoodmall.utils.FileUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 未使用文件清理定时任务
 */
@Component
@Slf4j
@AllArgsConstructor
public class FileCleanupTask {

    private final SysFileService sysFileService;

    private final FileUtils fileUtils;

    /**
     * 每天凌晨 3 点执行一次
     * cron 表达式说明：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOrphanFiles() {
        log.info(">>> 开始清理未使用文件任务 - 当前时间: {}", LocalDateTime.now());

        // 查询条件：is_used = 0 且 创建时间超过 24 小时
        LocalDateTime thresholdTime = LocalDateTime.now().minusDays(1);

        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFile::getIsUsed, 0)
                .lt(SysFile::getCreateTime, thresholdTime);

        List<SysFile> orphanFiles = sysFileService.list(wrapper);

        if (orphanFiles.isEmpty()) {
            log.info(">>> 暂无需要清理的未使用文件。");
            return;
        }

        int successCount = 0;
        for (SysFile file : orphanFiles) {
            try {
                // 执行物理删除
                fileUtils.deletePhysicalFile(file.getFilePath());

                // 删除数据库中的记录
                sysFileService.removeById(file.getId());

                successCount++;
                log.info("已清理未使用文件: {}", file.getFilePath());
            } catch (Exception e) {
                log.error("清理文件失败: {}，错误信息: {}", file.getFilePath(), e.getMessage());
            }
        }

        log.info(">>> 清理任务结束。成功释放 {} 个文件资源。", successCount);
    }
}
