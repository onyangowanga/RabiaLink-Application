package com.codewith.RabiaLinkApp.invoices.repository;

import com.codewith.RabiaLinkApp.invoices.domain.SupplierInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierInvoiceItemRepository extends JpaRepository<SupplierInvoiceItem, Long> {
}
