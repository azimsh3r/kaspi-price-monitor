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

    @Query("SELECT COUNT(*) FROM Order WHERE orderStatus = :orderStatus and createdAt BETWEEN :startDate AND :endDate")
    Integer countAllByOrderStatus(@Param("orderStatus") OrderStatus orderStatus,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(totalPrice) FROM Order WHERE orderStatus = :orderStatus and createdAt BETWEEN :startDate AND :endDate")
    Integer findRevenueByOrderStatus(@Param("orderStatus") OrderStatus orderStatus,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate
    );

    @Query("SELECT e FROM Order e WHERE e.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);
}
