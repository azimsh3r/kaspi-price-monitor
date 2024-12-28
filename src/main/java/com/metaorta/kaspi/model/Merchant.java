package com.metaorta.kaspi.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "merchant")
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name="name")
    private String name;

    @Column(name="merchant_id")
    private String merchantId;

    @Column(name="mc_sid")
    private String mc_sid;

    @Column(name="session_expires_at")
    private LocalDateTime sessionExpiresAt;

    @Column(name="username")
    private String username;

    @Column(name="password")
    private String password;

    @Column(name="price_last_raised_at")
    private LocalDateTime priceLastRaise;

    @Column(name="is_price_update_running")
    private Boolean isPriceUpdateRunning;

    @OneToMany(mappedBy = "merchant")
    private List<Product> productList;

    @Column(name="is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "merchant")
    private List<Order> orders;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
