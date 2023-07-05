package com.hy.im.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @ClassName ImApplication
 * description:
 * yao create 2023年06月29日
 * version: 1.0
 */
@SpringBootApplication(scanBasePackages = {"com.hy.im.service","com.hy.im.common"})
@MapperScan("com.hy.im.service.*.dao.mapper")
public class ImApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImApplication.class,args);
    }
}
