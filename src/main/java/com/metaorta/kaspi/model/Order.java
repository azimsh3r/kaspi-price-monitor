package com.metaorta.kaspi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.metaorta.kaspi.enums.OrderStatus;
import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.*;
import org.hibernate.annotations.Cascade;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name= "\"order\"")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    @JsonIgnore
    private Integer id;

    @Column(name="order_id")
    private String orderId;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="total_price")
    private Integer totalPrice;

    @Column(name="preorder")
    private Boolean preOrder;

    @Column(name="customer_phone_number")
    private String customerPhoneNumber;

    @Column(name="customer_name")
    private String customerName;

    @OneToMany(mappedBy = "order")
    @Cascade(org.hibernate.annotations.CascadeType.PERSIST)
    @JsonManagedReference
    private List<OrderEntry> orderEntries;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne
    @JoinColumn(name="merchant_id", referencedColumnName = "id")
    private Merchant merchant;
}
