package com.stock.stockDB;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "stock_daily_price",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_symbol_trade_date",
                        columnNames = {
                                "symbol",
                                "trade_date"
                        }
                )
        }
)
public class StockDailyPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "symbol", nullable = false)
    private String symbol;
    @Column(name = "trade_date", nullable = false)
    private String tradeDate;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private Long volume;
    @Column(length = 50)
    private String exchange;

    protected StockDailyPrice() {
    }

    public StockDailyPrice(
            String symbol,
            BigDecimal openPrice,
            BigDecimal highPrice,
            BigDecimal lowPrice,
            BigDecimal closePrice,
            Long volume,
            String exchange,
            String tradeDate) {
        this.symbol = symbol;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.exchange = exchange;
        this.tradeDate = tradeDate;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public Long getVolume() {
        return volume;
    }

    public String getExchange() {
        return exchange;
    }

    public String getTradeDate() {
        return tradeDate;
    }
}
