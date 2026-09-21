package com.stock.controller;


import com.stock.dto.dtoResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@Slf4j
@RequestMapping("/stocks")
public class FirstTestController {
    /**
     * 创建RestClient连接对象
     */
    private final RestClient restClient;
    @Value("${marketstack.api}")
    private String accessApiKey;

    public FirstTestController(@Value("${marketstack.base-url}")
                               String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }


    @GetMapping("/ping")
    public String Test(){
        return "service is running!";
    }

    /**
     * 拼接url地址
     *
     * @param symbols
     * @return
     */
    @GetMapping("/{symbols}")
    public  dtoResponse stockTest(@PathVariable String symbols){
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/eod")
                        .queryParam("access_key",accessApiKey)
                        .queryParam("symbols",symbols)
                        .build())
                .retrieve().body(dtoResponse.class);
    }
}
