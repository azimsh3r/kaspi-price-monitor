package com.metaorta.kaspi.repository;

import com.metaorta.kaspi.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Integer> {
    Optional<Merchant> findByMerchantId(String id);
}
