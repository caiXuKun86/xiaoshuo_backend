package com.kun.service.shelf;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.kun.service.shelf.mapper")
@SpringBootApplication
public class ShelfApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShelfApplication.class);
    }
}
