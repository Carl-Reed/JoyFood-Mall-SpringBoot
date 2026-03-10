package com.lpw.joyfoodmall.component;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.lpw.joyfoodmall.utils.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {

        // 自动填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());

        // 自动填充修改时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 自动填充操作人
        String currentUserName = SecurityUtils.getCurrentUsername();
        this.strictInsertFill(metaObject, "createBy", String.class, currentUserName);
        this.strictInsertFill(metaObject, "updateBy", String.class, currentUserName);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时修改 updateTime
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateBy", SecurityUtils.getCurrentUsername(), metaObject);
    }
}
