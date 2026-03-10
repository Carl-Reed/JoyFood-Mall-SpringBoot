package com.lpw.joyfoodmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.lpw.joyfoodmall.mapper")
public class JoyFoodMallApplication {

	public static void main(String[] args) {
		SpringApplication.run(JoyFoodMallApplication.class, args);
	}

}
