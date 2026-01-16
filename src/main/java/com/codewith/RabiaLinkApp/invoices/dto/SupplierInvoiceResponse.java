package com.codewith.RabiaLinkApp.invoices.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SupplierInvoiceResponse {
    private Long id;
    private String supplierName;
    private String supplierInvoiceNumber;
    private LocalDateTime invoiceDate;
    private List<SupplierInvoiceItem> items;

    public static class SupplierInvoiceItem {
        public Long productId;
        public String productName;
        public Integer quantity;
        public java.math.BigDecimal unitPrice;
        public java.math.BigDecimal lineTotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierInvoiceNumber() { return supplierInvoiceNumber; }
    public void setSupplierInvoiceNumber(String supplierInvoiceNumber) { this.supplierInvoiceNumber = supplierInvoiceNumber; }
    public LocalDateTime getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDateTime invoiceDate) { this.invoiceDate = invoiceDate; }
    public List<SupplierInvoiceItem> getItems() { return items; }
    public void setItems(List<SupplierInvoiceItem> items) { this.items = items; }
}
