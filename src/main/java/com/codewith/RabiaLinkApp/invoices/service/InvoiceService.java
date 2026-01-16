package com.codewith.RabiaLinkApp.invoices.service;

import java.util.List;

import com.codewith.RabiaLinkApp.invoices.dto.InvoiceResponse;

public interface InvoiceService {

    InvoiceResponse createInvoice(Long orderId);
    
    // Create a client-facing invoice from an existing supplier invoice
    InvoiceResponse createInvoiceFromSupplierInvoice(Long supplierInvoiceId);

    InvoiceResponse getInvoiceById(Long invoiceId);

    List<InvoiceResponse> getAllInvoices();

    List<InvoiceResponse> getInvoicesByClientId(Long clientId);

    List<InvoiceResponse> getInvoicesByOrderId(Long orderId);

    InvoiceResponse updateInvoiceStatus(Long invoiceId, String status);

    void deleteInvoice(Long invoiceId);
}
