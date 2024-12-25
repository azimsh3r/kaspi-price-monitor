package com.metaorta.kaspi.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @JacksonXmlProperty(localName = "model")
    @Column(name="name")
    private String name;

    @Column(name="min_price")
    private int minPrice;

    @Column(name="max_price")
    private int maxPrice;

    @JacksonXmlProperty(localName = "price")
    @Column(name="current_price")
    private int currentPrice;

    @JacksonXmlProperty(isAttribute = true)
    @Column(name="sku")
    private String sku;

    @ManyToOne
    @JoinColumn(name = "merchant_id", referencedColumnName = "id")
    private Merchant merchant;

    @Column(name="active")
    private Boolean active;
}
