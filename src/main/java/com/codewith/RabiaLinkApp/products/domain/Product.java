package com.codewith.RabiaLinkApp.products.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String unit; 
    // e.g. "TON", "CUBIC_METER"

    @Column(nullable = false)
    private BigDecimal defaultMarkup;

    @Column(nullable = false)
    private boolean active = true;

    // ===== Constructors =====
    public Product() {}

    public Product(String name, String unit, BigDecimal defaultMarkup) {
        this.name = name;
        this.unit = unit;
        this.defaultMarkup = defaultMarkup;
    }

    // ===== Getters & Setters =====
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getDefaultMarkup() {
        return defaultMarkup;
    }

    public void setDefaultMarkup(BigDecimal defaultMarkup) {
        this.defaultMarkup = defaultMarkup;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

