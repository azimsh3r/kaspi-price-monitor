package com.metaorta.kaspi.repository;

import com.metaorta.kaspi.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query("FROM Product where merchant.id = :merchantId")
    List<Product> findAllByMerchantId(@Param("merchantId") Integer merchantId);

    @Query("FROM Product where sku = :sku")
    Optional<Product> findBySku(@Param("sku") String sku);
}
