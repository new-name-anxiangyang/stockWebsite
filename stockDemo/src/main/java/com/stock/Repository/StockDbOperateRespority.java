package com.stock.Repository;

import com.stock.dto.dtoResponse;
import com.stock.stockDB.StockDailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockDbOperateRespority extends JpaRepository<StockDailyPrice, Long> {
    /**
     * 从数据库中查询所有信息
     * @param symbol
     * @return
     */
    List<StockDailyPrice> findBySymbol(String symbol);
    /**
     * 查询参数是否存在在数据库
     * @param symbol
     * @param tradedate
     * @return
     */
    boolean existsBySymbolAndTradeDate(String symbol, String tradedate);

    List<StockDailyPrice> findBySymbolOrderByTradeDateDesc(String symbol);
}
