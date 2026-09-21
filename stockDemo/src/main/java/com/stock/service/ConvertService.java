package com.stock.service;


import com.stock.Repository.StockDbOperateRespority;
import com.stock.dto.dtoResponse;
import com.stock.dto.stockDto;
import com.stock.stockDB.StockDailyPrice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class ConvertService {
    /**
     * 创建RestClient连接对象
     */
    private final RestClient restClient;
    /**
     * 注入jpa接口
     */
    private final StockDbOperateRespority respority;
    @Value("${marketstack.api}")
    private String accessApiKey;
    /**
     * 创建restClient对象以实现本地服务与远程服务的HTTP链接
     * @param baseUrl
     * @param respority
     */
    public ConvertService(@Value("${marketstack.base-url}")
                               String baseUrl, StockDbOperateRespority respority) {
        this.restClient = RestClient.create(baseUrl);
        this.respority = respority;
    }


    /**
     * 拼接url地址
     *
     * @param symbols
     * @return
     */
//    public dtoResponse stockService(String symbols){
//        return restClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/eod")
//                        .queryParam("access_key",accessApiKey)
//                        .queryParam("symbols",symbols)
//                        .build())
//                .retrieve().body(dtoResponse.class);
//    }

    /**
     * 新增将返回的json数据添加进表单并保存到mysql
     * @param symbols
     * @return
     */
    public dtoResponse getAndSaveStock(String symbols){
        dtoResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/eod")
                        .queryParam("access_key",accessApiKey)
                        .queryParam("symbols",symbols)
                        .build())
                .retrieve().body(dtoResponse.class);
        List<StockDailyPrice> prices = response.data()
                .stream()
                .map(this::convertToEntity)
                .toList();
        respority.saveAll(prices);
        return response;
    }
    private StockDailyPrice convertToEntity(stockDto data) {
        return new StockDailyPrice(
                data.getSymbol(),
                data.getOpen(),
                data.getHigh(),
                data.getLow(),
                data.getClose(),
                data.getVolume(),
                data.getExchange(),
                data.getDate()
        );
    }

    public List<StockDailyPrice> selectDB(String symbol) {
        return respority.findBySymbol(symbol);
    }
}
