package com.stock.controller;


import com.stock.dto.dtoResponse;
import com.stock.service.ConvertService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

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
    public dtoResponse convertControler(@PathVariable String symbols){
        return convertService.stockService(symbols);
    }
}
