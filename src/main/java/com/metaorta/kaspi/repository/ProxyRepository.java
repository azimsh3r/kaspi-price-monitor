package com.metaorta.kaspi.repository;

import com.metaorta.kaspi.model.Proxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProxyRepository extends JpaRepository<Proxy, Integer> {

    List<Proxy> findByMerchantId(int merchantId);
}
