package com.stock.Service;


import com.stock.Repository.StockDbOperateRespority;
import com.stock.Vo.dtoResponseVo;
import com.stock.Dto.stockDto;
import com.stock.Vo.stockHistoryResponseVo;
import com.stock.Entity.StockDailyPrice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final StockDbOperateRespority repository;
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
        this.repository = respority;
    }
    /**
     * 拼接url地址
     *
     * @param symbols
     * @return
     */
//    public dtoResponseVo stockService(String symbols){
//        return restClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/eod")
//                        .queryParam("access_key",accessApiKey)
//                        .queryParam("symbols",symbols)
//                        .build())
//                .retrieve().body(dtoResponseVo.class);
//    }

    /**
     * 新增将返回的json数据添加进表单并保存到mysql
     * @param symbols
     * @return
     */
    private dtoResponseVo fetchFromMarketstack(String symbols) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/eod")
                        .queryParam("access_key", accessApiKey)
                        .queryParam("symbols", symbols)
                        .build())
                .retrieve()
                .body(dtoResponseVo.class);
    }
    public List<stockHistoryResponseVo> getAndSaveStock(String symbols){
            List<StockDailyPrice> savedData =
                    repository.findBySymbolOrderByTradeDateAsc(symbols);

            if (savedData.isEmpty()) {
                dtoResponseVo response =
                        fetchFromMarketstack(symbols);
                saveNewData(response);
                savedData =
                        repository.findBySymbolOrderByTradeDateAsc(symbols);
            }
            return savedData.stream()
                    .map(this::convertToResponse)
                    .toList();

    }
    /**
     * 从 marketstack API 拉取股票数据并保存到数据库
     */
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
//    private void saveStockFromApi(String symbols) {
//        dtoResponseVo response = restClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/eod")
//                        .queryParam("access_key", accessApiKey)
//                        .queryParam("symbols", symbols)
//                        .build())
//                .retrieve().body(dtoResponseVo.class);
//        List<StockDailyPrice> prices = response.data()
//                .stream()
//                .filter(data -> !respority.existsBySymbolAndTradeDate(
//                        data.getSymbol(),
//                        data.getDate()
//                ))
//                .map(this::convertToEntity)
//                .toList();
//        if (!prices.isEmpty()) {
//            respority.saveAll(prices);
//        }
//    }
    /**
     * 从数据库中查询参数对应的数据
     * @param symbol
     * @return
     */
    public List<StockDailyPrice> selectDB(String symbol) {
        return repository.findBySymbol(symbol);
    }
    /**
     * 查询历史数据
     *
     * @param symbol
     * @return
     */
    public Page<stockHistoryResponseVo> findStockHistory(String symbol, int page, int size) {
        PageRequest request = PageRequest.of(page, size);
        Page<StockDailyPrice> stockPage = repository
                .findBySymbolOrderByTradeDateAsc(symbol,request);
        return stockPage.map(this::convertToResponse);
    }
    private stockHistoryResponseVo convertToResponse(
            StockDailyPrice price) {
        return new stockHistoryResponseVo(
                price.getSymbol(),
                price.getOpenPrice(),
                price.getHighPrice(),
                price.getLowPrice(),
                price.getClosePrice(),
                price.getVolume(),
                price.getExchange(),
                price.getTradeDate()
        );
    }

    /**
     * 统一保存逻辑
     * @param response
     */
    private void saveNewData(dtoResponseVo response){
        List<StockDailyPrice> newPrices =
                response.data()
                        .stream()
                        .filter(data ->
                                !repository.existsBySymbolAndTradeDate(
                                        data.getSymbol(),
                                        data.getDate()
                                )
                        )
                        .map(this::convertToEntity)
                        .toList();

        if (!newPrices.isEmpty()) {
            repository.saveAll(newPrices);
        }

    }

    /**
     * 刷新行情功能
     * @param symbols
     * @return
     */
    public List<stockHistoryResponseVo> refreshStock(String symbols){
        dtoResponseVo dtoResponseVo = fetchFromMarketstack(symbols);
        saveNewData(dtoResponseVo);
        List<StockDailyPrice> dateAsc = repository.findBySymbolOrderByTradeDateAsc(symbols);
        return dateAsc.stream().map(this::convertToResponse).toList();
    }
}
