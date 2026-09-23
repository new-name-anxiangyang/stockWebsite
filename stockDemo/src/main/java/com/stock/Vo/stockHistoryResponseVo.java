package com.stock.Vo;

import java.math.BigDecimal;

public record stockHistoryResponseVo(String symbol,
                                     BigDecimal openPrice,
                                     BigDecimal highPrice,
                                     BigDecimal lowPrice,
                                     BigDecimal closePrice,
                                     Long volume,
                                     String exchange,
                                     String tradeDate) {

}
