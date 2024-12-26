package com.metaorta.kaspi.repository;

import com.metaorta.kaspi.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("SELECT o.orderId FROM Order o WHERE o.orderId IN :orderIds")
    List<String> findOrderIdsByOrderIdIn(@Param("orderIds") List<String> orderIds);
}
