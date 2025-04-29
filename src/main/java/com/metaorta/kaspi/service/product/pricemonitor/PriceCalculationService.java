package com.metaorta.kaspi.service.product.pricemonitor;

import com.metaorta.kaspi.model.Competitor;
import com.metaorta.kaspi.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceCalculationService {
    public int calculateNewPrice(Product product, List<Competitor> competitors, boolean raisePrices) {
        if (competitors.isEmpty()) return product.getMaxPrice();
        int newPrice = product.getCurrentPrice();

        for (Competitor c : competitors) {
            if (!c.getMerchantSku().equals(product.getSku())
                    && c.getPrice() < newPrice
                    && product.getMinPrice() < c.getPrice()) {
                newPrice = c.getPrice() - 1;
                break;
            }
        }

        if (raisePrices) {
            int finalNewPrice = newPrice;
            int nextLowest = competitors.stream()
                    .filter(c -> !c.getMerchantSku().equals(product.getSku()))
                    .mapToInt(Competitor::getPrice)
                    .filter(p -> p > finalNewPrice)
                    .min()
                    .orElse(Integer.MAX_VALUE);

            newPrice = Math.min(nextLowest - 1, product.getMaxPrice());
        }

        return newPrice;
    }

    public int calcDefaultMinPrice(int currentPrice) {
        return currentPrice * 9 / 10;
    }

    public int calcDefaultMaxPrice(int currentPrice) {
        return currentPrice + currentPrice / 10;
    }
}
