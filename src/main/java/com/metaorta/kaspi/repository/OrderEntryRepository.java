package com.metaorta.kaspi.repository;

import com.metaorta.kaspi.model.OrderEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderEntryRepository extends JpaRepository<OrderEntry, Integer> {
}
