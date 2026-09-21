package com.stock.Repository;

import com.stock.dto.dtoResponse;
import com.stock.stockDB.StockDailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockDbOperateRespority extends JpaRepository<StockDailyPrice, Long> {

    List<StockDailyPrice> findBySymbol(String symbol);
}
