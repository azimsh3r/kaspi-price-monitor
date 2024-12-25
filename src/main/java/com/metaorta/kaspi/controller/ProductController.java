package com.metaorta.kaspi.controller;

import com.metaorta.kaspi.dto.ProductDTO;
import com.metaorta.kaspi.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductDTO> getProducts() {
        return productService.getFetchAllProducts("18420073","d639f7a4-6b59-4c7d-be24-66b71f5f217e");
    }



}

