package com.market.online;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @className: UserServiceApplication
 * @author: LZX
 * @date: 2025/2/9 14:22
 * @Version: 1.0
 * @description:
 */
@SpringBootApplication
@MapperScan("com.market.online.mapper")
@EnableTransactionManagement
@EnableDiscoveryClient
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}


