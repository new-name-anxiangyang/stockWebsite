package com.stock.Controller;
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

     private final ConvertService convertService;

    /**
     * 自动注入service
     * @param convertService
     */
    public FirstTestController(ConvertService convertService){
        this.convertService = convertService;
    }

    @GetMapping("/{symbols}")
    public List<stockHistoryResponseVo> convertControler(@PathVariable String symbols){
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if(symbol.isBlank()){throw new IllegalArgumentException("股票代码为空");}
        else if (page < 0) {throw new IllegalArgumentException("页码不能小于0");}
        else if (size < 1 || size > 100){throw new IllegalArgumentException("数量必须在1到100之间");}
        return convertService.findStockHistory(symbol,page,size);
    }

    @PostMapping("/{symbol}/refresh")
    public List<stockHistoryResponseVo> refreshStock(@PathVariable String symbol){
        return convertService.refreshStock(symbol);
    }
}
