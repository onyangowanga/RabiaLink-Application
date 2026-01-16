package com.codewith.RabiaLinkApp.invoices.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SupplierInvoiceRequest {

    private Long orderId;
    private Long clientId;  // Required when orderId is null (for auto-generated orders)
    private String supplierName;
    private String supplierInvoiceNumber;
    private LocalDateTime invoiceDate;
    private List<SupplierInvoiceItemRequest> items;
    private String createdBy;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
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

    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public List<SupplierInvoiceItemRequest> getItems() {
        return items;
    }

    public void setItems(List<SupplierInvoiceItemRequest> items) {
        this.items = items;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public static class SupplierInvoiceItemRequest {
        private Long productId;
        private Integer quantity;
        private BigDecimal unitPrice; // supplier price

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
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
    }
}
