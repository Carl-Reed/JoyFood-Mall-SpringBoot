package com.lpw.joyfoodmall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.entity.DTO.PageParams;
import com.lpw.joyfoodmall.entity.SysFile;
import com.lpw.joyfoodmall.service.FileService;
import com.lpw.joyfoodmall.service.SysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final SysFileService sysFileService;

    @PostMapping("/upload")
    public Result<String> upload (String folder, MultipartFile file){
        return Result.success(fileService.executeUpload(folder, file));
    }

    @GetMapping("/record/list")
    public Result<?> recordList (
            PageParams params,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) Integer isUsed)
    {
        Page<SysFile> page = new Page<>(params.getPage(), params.getLimit());

        // 构建查询条件
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();

        // 如果搜索内容不为空，则进行模糊查询
        if (StringUtils.hasText(searchText)) {
            wrapper.like(SysFile::getFilePath, searchText);
        }

        // 状态筛选
        if (isUsed != null) {
            wrapper.eq(SysFile::getIsUsed, isUsed);
        }

        wrapper.orderByDesc(SysFile::getId);

        // 执行查询，返回全量 List
        IPage<SysFile> pageResult = sysFileService.page(page,wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("total",pageResult.getTotal());
        result.put("rows",pageResult.getRecords());

        return Result.success(result);
    }

    /** 单个文件记录清理 */
    @DeleteMapping("record/delete/{id}")
    public Result<String> deleteById(@PathVariable Long id) {

        if (sysFileService.deleteUnusedFile(id)) {
            return Result.message("文件已成功物理删除");
        } else {
            return Result.error("删除失败：文件可能正在使用中，或上传未满24小时");
        }
    }

    /** 一键清理所有过期未使用文件 */
    @PostMapping("record/clear-unused")
    public Result<Map<String, Object>> clearAllUnused() {
        Map<String, Object> report = sysFileService.clearAllUnusedFiles();
        return Result.success(report);
    }
}
