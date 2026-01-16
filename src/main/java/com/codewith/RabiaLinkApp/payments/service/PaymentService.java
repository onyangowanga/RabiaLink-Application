package com.codewith.RabiaLinkApp.payments.service;

import com.codewith.RabiaLinkApp.payments.dto.PaymentRequest;
import com.codewith.RabiaLinkApp.payments.dto.PaymentResponse;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    /**
     * Process a new payment
     * - Validates invoice/supplier invoice exists
     * - Prevents overpayment
     * - Updates invoice status (PAID / PARTIALLY_PAID)
     * - Returns payment confirmation
     */
    PaymentResponse processPayment(PaymentRequest request);

    /**
     * Get all payments for a client invoice
     */
    List<PaymentResponse> getPaymentsByInvoice(Long invoiceId);

    /**
     * Get all payments for a supplier invoice
     */
    List<PaymentResponse> getPaymentsBySupplierInvoice(Long supplierInvoiceId);

    /**
     * Get total amount paid for an invoice
     */
    BigDecimal getTotalPaidForInvoice(Long invoiceId);

    /**
     * Get total amount paid for a supplier invoice
     */
    BigDecimal getTotalPaidForSupplierInvoice(Long supplierInvoiceId);

    /**
     * Get payment by ID
     */
    PaymentResponse getPaymentById(Long paymentId);

    /**
     * Reverse a completed payment
     */
    PaymentResponse reversePayment(Long paymentId);
}
