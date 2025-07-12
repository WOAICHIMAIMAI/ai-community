package com.zheng.aicommunitybackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.zheng.aicommunitybackend.mapper")

public class AiCommunityBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCommunityBackendApplication.class, args);
    }

}
