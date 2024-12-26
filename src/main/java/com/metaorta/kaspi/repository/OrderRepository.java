package com.metaorta.kaspi.repository;

import com.metaorta.kaspi.enums.OrderStatus;
import com.metaorta.kaspi.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("SELECT o.orderId FROM Order o WHERE o.orderId IN :orderIds")
    List<String> findOrderIdsByOrderIdIn(@Param("orderIds") List<String> orderIds);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :orderStatus AND o.createdAt BETWEEN :startDate AND :endDate")
    Integer countAllByOrderStatus(@Param("orderStatus") OrderStatus orderStatus,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.orderStatus = :orderStatus AND o.createdAt BETWEEN :startDate AND :endDate")
    Integer findRevenueByOrderStatus(@Param("orderStatus") OrderStatus orderStatus,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM Order e WHERE e.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);

    @Query("SELECT o.createdAt FROM Order o ORDER BY o.createdAt DESC")
    LocalDateTime findLastOrderCreatedAt();
}
