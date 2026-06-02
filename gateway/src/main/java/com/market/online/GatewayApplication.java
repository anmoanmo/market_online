package com.market.online;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient//开启nacos服务注册和发现
public class GatewayApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(GatewayApplication.class, args);
    }
}