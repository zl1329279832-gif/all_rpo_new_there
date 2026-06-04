package com.bakery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.bakery.mapper")
public class BakeryApplication {

    public static void main(String[] args) {
        SpringApplication.run(BakeryApplication.class, args);
        System.out.println("\n===============================================");
        System.out.println("  连锁烘焙门店生产与临期管理系统启动成功!");
        System.out.println("  接口文档: http://localhost:8080/api/doc.html");
        System.out.println("===============================================\n");
    }
}
