package com.metaorta.kaspi.util;

import com.metaorta.kaspi.dto.ProductDTO;
import com.metaorta.kaspi.model.Merchant;
import com.metaorta.kaspi.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductFactory {
    public static Product create(ProductDTO dto, Merchant merchant) {
        Product product = new Product();
        product.setSku(dto.getSku());
        product.setName(dto.getModel());
        product.setCurrentPrice(dto.getPrice());
        product.setMerchant(merchant);
        product.setActive(true);
        return product;
    }
}
