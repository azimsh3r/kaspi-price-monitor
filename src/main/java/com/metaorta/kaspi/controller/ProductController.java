package com.metaorta.kaspi.controller;

import com.metaorta.kaspi.service.product.ProductFetchService;
import com.metaorta.kaspi.service.product.pricemonitor.PriceUpdateService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductFetchService productFetchService;
    private final PriceUpdateService priceUpdateService;

    public ProductController(ProductFetchService productFetchService, PriceUpdateService priceUpdateService) {
        this.productFetchService = productFetchService;
        this.priceUpdateService = priceUpdateService;
    }
}
