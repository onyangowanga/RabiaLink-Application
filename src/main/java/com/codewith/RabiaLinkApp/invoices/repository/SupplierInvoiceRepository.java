package com.codewith.RabiaLinkApp.invoices.repository;

import com.codewith.RabiaLinkApp.invoices.domain.SupplierInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierInvoiceRepository extends JpaRepository<SupplierInvoice, Long> {
    List<SupplierInvoice> findByOrderId(Long orderId);
    SupplierInvoice findBySupplierInvoiceNumber(String supplierInvoiceNumber);
}
