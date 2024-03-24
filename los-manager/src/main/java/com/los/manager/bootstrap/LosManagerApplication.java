package com.los.manager.bootstrap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@MapperScan("com.los.service.mapper")
public class LosManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LosManagerApplication.class, args);
    }
    // TODO 2024/3/23 : 处理 fastjson2 ,mp , knife4j的配置
}
