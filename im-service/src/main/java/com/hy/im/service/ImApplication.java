package com.hy.im.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @ClassName ImApplication
 * description:
 * yao create 2023年06月29日
 * version: 1.0
 */
@SpringBootApplication(scanBasePackages = {"com.hy.im.service","com.hy.im.common"})
public class ImApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImApplication.class,args);
    }
}
