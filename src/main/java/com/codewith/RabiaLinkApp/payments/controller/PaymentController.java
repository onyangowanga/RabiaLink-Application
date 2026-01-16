package com.codewith.RabiaLinkApp.payments.controller;

import com.codewith.RabiaLinkApp.payments.dto.PaymentRequest;
import com.codewith.RabiaLinkApp.payments.dto.PaymentResponse;
import com.codewith.RabiaLinkApp.payments.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/payments
     * Process a new payment (client, supplier, or partner contribution)
     */
    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/payments/{id}
     * Retrieve payment by ID
     */
    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/payments/invoice/{invoiceId}
     * Retrieve all payments for a client invoice
     */
    @GetMapping("/invoice/{invoiceId}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<List<PaymentResponse>> getPaymentsByInvoice(@PathVariable Long invoiceId) {
        List<PaymentResponse> responses = paymentService.getPaymentsByInvoice(invoiceId);
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/payments/supplier-invoice/{supplierInvoiceId}
     * Retrieve all payments for a supplier invoice
     */
    @GetMapping("/supplier-invoice/{supplierInvoiceId}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<List<PaymentResponse>> getPaymentsBySupplierInvoice(@PathVariable Long supplierInvoiceId) {
        List<PaymentResponse> responses = paymentService.getPaymentsBySupplierInvoice(supplierInvoiceId);
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/payments/invoice/{invoiceId}/total-paid
     * Get total amount paid for an invoice
     */
    @GetMapping("/invoice/{invoiceId}/total-paid")
    public ResponseEntity<BigDecimal> getTotalPaidForInvoice(@PathVariable Long invoiceId) {
        BigDecimal totalPaid = paymentService.getTotalPaidForInvoice(invoiceId);
        return ResponseEntity.ok(totalPaid);
    }

    /**
     * GET /api/payments/supplier-invoice/{supplierInvoiceId}/total-paid
     * Get total amount paid for a supplier invoice
     */
    @GetMapping("/supplier-invoice/{supplierInvoiceId}/total-paid")
    public ResponseEntity<BigDecimal> getTotalPaidForSupplierInvoice(@PathVariable Long supplierInvoiceId) {
        BigDecimal totalPaid = paymentService.getTotalPaidForSupplierInvoice(supplierInvoiceId);
        return ResponseEntity.ok(totalPaid);
    }

    /**
     * POST /api/payments/{id}/reverse
     * Reverse a completed payment
     */
    @PostMapping("/{id}/reverse")
    public ResponseEntity<PaymentResponse> reversePayment(@PathVariable Long id) {
        PaymentResponse response = paymentService.reversePayment(id);
        return ResponseEntity.ok(response);
    }
}
