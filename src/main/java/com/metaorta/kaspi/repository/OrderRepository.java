package com.metaorta.kaspi.repository;

import com.metaorta.kaspi.enums.OrderStatus;
import com.metaorta.kaspi.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("SELECT o.orderId FROM Order o WHERE o.merchant.id = :merchantId AND o.orderId IN :orderIds")
    List<String> findOrderIdsByOrderIdInAndMerchantId(@Param("orderIds") List<String> orderIds, @Param("merchantId") Integer merchantId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :orderStatus AND o.merchant.id = :merchantId AND o.createdAt BETWEEN :startDate AND :endDate")
    Integer countAllByOrderStatusAndMerchantId(@Param("orderStatus") OrderStatus orderStatus,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               @Param("merchantId") Integer merchantId);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.orderStatus = :orderStatus AND o.merchant.id = :merchantId AND o.createdAt BETWEEN :startDate AND :endDate")
    Integer findRevenueByOrderStatusAndMerchantId(@Param("orderStatus") OrderStatus orderStatus,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate,
                                                  @Param("merchantId") Integer merchantId);

    @Query("SELECT e FROM Order e WHERE e.merchant.id = :merchantId AND e.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findByCreatedAtBetweenAndMerchantId(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate,
                                                    @Param("merchantId") Integer merchantId,
                                                    Pageable pageable);

    @Query("SELECT o.createdAt FROM Order o WHERE o.merchant.id = :merchant_id ORDER BY o.createdAt DESC")
    LocalDateTime findLastOrderCreatedAtAndMerchantId(@Param("merchant_id") Integer merchantId);
}
