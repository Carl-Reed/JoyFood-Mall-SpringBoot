package com.lpw.joyfoodmall.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig {
    @Value("${snowflake.worker-id:1}")
    private long workerId;

    @Value("${snowflake.datacenter-id:1}")
    private long dataCenterId;

    @Bean
    public Snowflake snowflake() {
        // 传入机器ID和数据中心ID创建雪花算法实例
        return IdUtil.getSnowflake(workerId, dataCenterId);
    }
}
