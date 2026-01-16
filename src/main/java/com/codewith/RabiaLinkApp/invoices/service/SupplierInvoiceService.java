package com.codewith.RabiaLinkApp.invoices.service;

import com.codewith.RabiaLinkApp.invoices.dto.SupplierInvoiceRequest;
import com.codewith.RabiaLinkApp.invoices.dto.SupplierInvoiceResponse;

public interface SupplierInvoiceService {
    SupplierInvoiceResponse createSupplierInvoice(SupplierInvoiceRequest request);
}
