package com.ragflow.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan("com.ragflow.admin.mapper")
public class RagflowAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagflowAdminApplication.class, args);
    }
}
