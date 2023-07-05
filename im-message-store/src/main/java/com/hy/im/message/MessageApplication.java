package com.hy.im.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @ClassName MessageApplication
 * description: 消息持久化服务
 * yao create 2023年07月04日
 * version: 1.0
 */
@SpringBootApplication
@MapperScan("com.hy.im.message.dao.mapper")
public class MessageApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageApplication.class,args);
    }
}
