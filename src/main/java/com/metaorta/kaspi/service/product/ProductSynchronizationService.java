package com.metaorta.kaspi.service.product;

import com.metaorta.kaspi.dto.ProductDTO;
import com.metaorta.kaspi.model.Merchant;
import com.metaorta.kaspi.model.Product;
import com.metaorta.kaspi.repository.ProductRepository;
import com.metaorta.kaspi.service.merchant.MerchantService;
import com.metaorta.kaspi.service.session.UserSessionService;
import com.metaorta.kaspi.util.ProductFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductSynchronizationService {
    private final ProductFetchService fetchService;
    private final ProductFactory productFactory;
    private final ProductRepository productRepository;
    private final MerchantService merchantService;
    private final UserSessionService sessionService;

    @Autowired
    public ProductSynchronizationService(ProductFetchService fetchService, ProductFactory productFactory, ProductRepository productRepository, MerchantService merchantService, UserSessionService sessionService) {
        this.fetchService = fetchService;
        this.productFactory = productFactory;
        this.productRepository = productRepository;
        this.merchantService = merchantService;
        this.sessionService = sessionService;
    }

    public void synchronizeProducts(int merchantId) {
        Merchant merchant = merchantService.getMerchantById(merchantId).orElseThrow(RuntimeException::new);
        String sessionId = sessionService.getUserSessionIdFromRedis(merchant.getUsername());

        List<ProductDTO> productDTOList = fetchService.getFetchAllProducts(merchant.getMerchantId(), sessionId);
        List<Product> existingProducts = productRepository.findAllByMerchantId(merchantId);

        Map<String, ProductDTO> productDTOMap = productDTOList.stream()
                .collect(Collectors.toMap(ProductDTO::getSku, Function.identity(), (e1, e2) -> e1));

        List<Product> productsToDelete = existingProducts.stream()
                .filter(p -> !productDTOMap.containsKey(p.getSku()))
                .toList();

        List<Product> toUpdate = new ArrayList<>();
        List<Product> toCreate = new ArrayList<>();

        for (ProductDTO dto : productDTOList) {
            Optional<Product> existing = existingProducts.stream()
                    .filter(p -> p.getSku().equals(dto.getSku()))
                    .findFirst();

            if (existing.isPresent()) {
                Product product = existing.get();
                product.setCurrentPrice(dto.getPrice());
                toUpdate.add(product);
            } else {
                toCreate.add(productFactory.create(dto, merchant));
            }
        }

        if (!productsToDelete.isEmpty()) productRepository.deleteAll(productsToDelete);
        if (!toUpdate.isEmpty()) productRepository.saveAll(toUpdate);
        if (!toCreate.isEmpty()) productRepository.saveAll(toCreate);
    }
}
