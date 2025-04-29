package com.metaorta.kaspi.service.product.pricemonitor;

import com.metaorta.kaspi.dto.ProductDTO;
import com.metaorta.kaspi.model.Competitor;
import com.metaorta.kaspi.model.Merchant;
import com.metaorta.kaspi.model.Product;
import com.metaorta.kaspi.model.Proxy;
import com.metaorta.kaspi.repository.ProductRepository;
import com.metaorta.kaspi.service.MerchantService;
import com.metaorta.kaspi.service.product.ProductFetchService;
import com.metaorta.kaspi.service.UserSessionService;
import com.metaorta.kaspi.util.ProductFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

@Service
public class PriceUpdateService {
    private final MerchantService merchantService;
    private final ProductRepository productRepository;
    private final PriceCalculationService priceCalculationService;
    private final CompetitorService competitorService;
    private final ProductFetchService productFetchService;
    private final UserSessionService userSessionService;

    public PriceUpdateService(
            MerchantService merchantService,
            ProductRepository productRepository,
            PriceCalculationService priceCalculationService,
            CompetitorService competitorService,
            ProductFetchService productFetchService, UserSessionService userSessionService
    ) {
        this.merchantService = merchantService;
        this.productRepository = productRepository;
        this.priceCalculationService = priceCalculationService;
        this.competitorService = competitorService;
        this.productFetchService = productFetchService;
        this.userSessionService = userSessionService;
    }

    public void updatePrices(int merchantId, BlockingQueue<Proxy> proxies) {
        Merchant merchant = merchantService.getMerchantById(merchantId)
                .orElseThrow(() -> new RuntimeException("Merchant not found: " + merchantId));
        if (merchant.getIsPriceUpdateRunning()) return;

        String userSessionId = userSessionService.getUserSessionIdFromRedis(String.valueOf(merchantId));
        List<ProductDTO> productList = productFetchService.getFetchAllProducts(merchant.getMerchantId(), userSessionId);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Semaphore semaphore = new Semaphore(5);
        boolean raisePrices = shouldRaisePrices(merchant);

        scheduler.scheduleWithFixedDelay(() -> {
            competitorService.manageCooldown(proxies);

            List<CompletableFuture<Void>> futures = productList.stream()
                    .map(dto -> CompletableFuture.runAsync(() -> {
                        try {
                            semaphore.acquire();
                            updateProductPrice(dto, merchant, proxies, raisePrices);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            semaphore.release();
                        }
                    }))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            if (raisePrices) {
                merchant.setPriceLastRaise(LocalDateTime.now());
                merchantService.save(merchant);
            }
        }, 0, 300, TimeUnit.SECONDS);
    }

    private void updateProductPrice(ProductDTO dto, Merchant merchant, BlockingQueue<Proxy> proxies, boolean raisePrices) {
        try {
            List<Competitor> competitors = competitorService.getCompetitors(dto.getSku(), proxies);
            Product product = productRepository.findBySku(dto.getSku()).orElseGet(() -> {
                Product p = ProductFactory.create(dto, merchant);
                productRepository.save(p);
                return p;
            });
            int newPrice = priceCalculationService.calculateNewPrice(product, competitors, raisePrices);
            product.setCurrentPrice(newPrice);
            productRepository.save(product);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean shouldRaisePrices(Merchant merchant) {
        return merchant.getPriceLastRaise() == null ||
                LocalDateTime.now().minusHours(5).isAfter(merchant.getPriceLastRaise());
    }
}
