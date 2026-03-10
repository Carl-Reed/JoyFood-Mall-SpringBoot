package com.lpw.joyfoodmall.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class MyWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectPath = System.getProperty("user.dir");
        // 物理路径指向 uploads/
        String absolutePath = projectPath + File.separator + "uploads" + File.separator;

        registry.addResourceHandler("/files/**") // 匹配所有子路径
                .addResourceLocations("file:" + absolutePath);
    }
}
