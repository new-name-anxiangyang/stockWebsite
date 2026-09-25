package com.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCaching//开启Spring缓存
public class StockDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockDemoApplication.class, args);
    }

}
