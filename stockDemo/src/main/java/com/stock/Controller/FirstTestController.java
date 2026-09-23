package com.stock.Controller;


import com.stock.Vo.dtoResponseVo;
import com.stock.Vo.stockHistoryResponseVo;
import com.stock.Service.ConvertService;
import com.stock.Entity.StockDailyPrice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/stocks")
public class FirstTestController {


    @GetMapping("/ping")
    public String Test(){
        return "service is running!";
    }
    @Autowired
     private ConvertService convertService;

    @GetMapping("/{symbols}")
    public dtoResponseVo convertControler(@PathVariable String symbols){
        log.info("转换服务 + {} + 已启动" ,symbols);
        return convertService.getAndSaveStock(symbols);
    }

    @GetMapping("/db/{symbol}")
    public List<StockDailyPrice> stlectDBControler(@PathVariable String symbol){
        log.info("转换服务 + {} + 已启动" ,symbol);
        return convertService.selectDB(symbol);
    }

    @GetMapping("/history/{symbol}")
    public Page<stockHistoryResponseVo> getStockHistory(
            @PathVariable String symbol,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return convertService.findStockHistory(symbol, start, end,page,size);
    }
}
