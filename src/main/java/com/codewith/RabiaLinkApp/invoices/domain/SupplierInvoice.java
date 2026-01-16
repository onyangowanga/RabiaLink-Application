package com.codewith.RabiaLinkApp.invoices.domain;

import com.codewith.RabiaLinkApp.orders.domain.Order;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supplier_invoices")
public class SupplierInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String supplierName;

    @Column(nullable = false, unique = true)
    private String supplierInvoiceNumber;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private LocalDateTime invoiceDate;

    @OneToMany(mappedBy = "supplierInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierInvoiceItem> items = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    // Audit fields
    private String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "supplierInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.codewith.RabiaLinkApp.payments.domain.Payment> payments = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierInvoiceNumber() {
        return supplierInvoiceNumber;
    }

    public void setSupplierInvoiceNumber(String supplierInvoiceNumber) {
        this.supplierInvoiceNumber = supplierInvoiceNumber;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public List<SupplierInvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<SupplierInvoiceItem> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public List<com.codewith.RabiaLinkApp.payments.domain.Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<com.codewith.RabiaLinkApp.payments.domain.Payment> payments) {
        this.payments = payments;
    }
}

