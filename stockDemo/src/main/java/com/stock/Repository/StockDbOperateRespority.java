package com.stock.Repository;

import com.stock.stockDB.StockDailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockDbOperateRespority extends JpaRepository<StockDailyPrice, Long> {
}
