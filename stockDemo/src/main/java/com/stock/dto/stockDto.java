package com.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 例子
 *       "open": 337.91,
 *       "high": 338.49,
 *       "low": 332.53,
 *       "close": 336.13,
 *       "volume": 86433100,
 *       "adj_high": 338.49,
 *       "adj_low": 332.53,
 *       "adj_close": 336.13,
 *       "adj_open": 337.91,
 *       "adj_volume": 86588203,
 *       "split_factor": 1,
 *       "dividend": 0,
 *       "name": "Apple Inc",
 *       "exchange_code": "NASDAQ",
 *       "asset_type": "Stock",
 *       "price_currency": "USD",
 *       "symbol": "AAPL",
 *       "exchange": "XNAS",
 *       "date": "2026-09-18T00:00:00+0000"
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class stockDto {
            BigDecimal open;
            BigDecimal high;
            BigDecimal low;
            BigDecimal close;
            Long volume;
            BigDecimal adj_high;
            BigDecimal adj_low;
            BigDecimal adj_close;
            BigDecimal adj_open;
            BigDecimal adj_volume;
            Integer split_factor;
            Integer dividend;
            String name;
            String exchange_code;
            String asset_type;
            String price_currency;
            String symbol;
            String exchange;
            String date;
}
