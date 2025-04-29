package com.metaorta.kaspi.controller;

import com.metaorta.kaspi.model.Proxy;
import com.metaorta.kaspi.service.ProxyService;
import com.metaorta.kaspi.service.product.pricemonitor.PriceUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final PriceUpdateService priceUpdateService;
    private final ProxyService proxyService;

    @Autowired
    public MerchantController(PriceUpdateService priceUpdateService, ProxyService proxyService) {
        this.priceUpdateService = priceUpdateService;
        this.proxyService = proxyService;
    }

    @PostMapping("/update-prices/{id}")
    public ResponseEntity<String> updatePrices(@PathVariable("id") Integer id) {
        List<Proxy> proxyList = proxyService.getAllProxiesByMerchantId(id);
        BlockingQueue<Proxy> proxyQueue = new LinkedBlockingQueue<>(proxyList);

        priceUpdateService.updatePrices(id, proxyQueue);
        return new ResponseEntity<String>("Price update initiated for merchant ID: " + id, HttpStatus.OK);
    }
}
