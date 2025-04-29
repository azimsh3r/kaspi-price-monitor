package com.metaorta.kaspi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="proxy")
@Data
public class Proxy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name="host")
    private String host;

    @Column(name="port")
    private Integer port;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id", name = "merchant_id")
    private Merchant merchant;

    @Transient
    private LocalDateTime cooldownStart;
}
