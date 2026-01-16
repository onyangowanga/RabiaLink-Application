package com.codewith.RabiaLinkApp.invoices.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * InvoiceItem represents a snapshot of order item details at invoice creation time.
 * Prices and quantities are frozen and never updated.
 */
@Entity
@Table(name = "invoice_items")
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    // Frozen snapshot of product information
    @Column(nullable = false)
    private String productName;

    // Frozen quantities and prices at creation time
    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice; // Immutable snapshot price

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal; // Immutable snapshot total

    /**
     * Defensive calculation: Ensures lineTotal is computed even if not explicitly set.
     * This prevents partial invoice items and ensures database consistency.
     */
    @PrePersist
    void calculateLineTotal() {
        if (this.lineTotal == null && this.unitPrice != null && this.quantity != null) {
            this.lineTotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
