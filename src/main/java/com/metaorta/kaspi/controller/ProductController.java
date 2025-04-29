package com.metaorta.kaspi.controller;

import com.metaorta.kaspi.dto.ProductDTO;
import com.metaorta.kaspi.service.product.ProductFetchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductFetchService productFetchService;

    public ProductController(ProductFetchService productFetchService) {
        this.productFetchService = productFetchService;
    }

    @GetMapping
    public List<ProductDTO> getProducts() {
        return productFetchService.getFetchAllProducts("18420073","02c516f7-5cc2-4f97-bc01-d79939b46092");
    }
}
