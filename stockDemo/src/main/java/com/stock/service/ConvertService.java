package com.stock.service;


import com.stock.dto.dtoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;

@Service
public class ConvertService {
    /**
     * 创建RestClient连接对象
     */
    private final RestClient restClient;
    @Value("${marketstack.api}")
    private String accessApiKey;

    public ConvertService(@Value("${marketstack.base-url}")
                               String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    /**
     * 拼接url地址
     *
     * @param symbols
     * @return
     */
    public dtoResponse stockService(String symbols){
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/eod")
                        .queryParam("access_key",accessApiKey)
                        .queryParam("symbols",symbols)
                        .build())
                .retrieve().body(dtoResponse.class);
    }
}
