package com.codewith.RabiaLinkApp.invoices.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import com.codewith.RabiaLinkApp.invoices.dto.InvoiceResponse;
import com.codewith.RabiaLinkApp.invoices.service.InvoiceService;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Create invoice from an order
     */
    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<InvoiceResponse> createInvoice(
            @RequestParam Long orderId
    ) {
        InvoiceResponse response = invoiceService.createInvoice(orderId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get a single invoice by ID
     */
    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    public ResponseEntity<InvoiceResponse> getInvoiceById(
            @PathVariable Long id
    ) {
        InvoiceResponse response = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all invoices
     */
    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        List<InvoiceResponse> responses = invoiceService.getAllInvoices();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get invoices by client ID
     */
    @GetMapping("/client/{clientId}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByClientId(
            @PathVariable Long clientId
    ) {
        List<InvoiceResponse> responses = invoiceService.getInvoicesByClientId(clientId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get invoices by order ID
     */
    @GetMapping("/order/{orderId}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByOrderId(
            @PathVariable Long orderId
    ) {
        List<InvoiceResponse> responses = invoiceService.getInvoicesByOrderId(orderId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Update invoice status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<InvoiceResponse> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        InvoiceResponse response = invoiceService.updateInvoiceStatus(id, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete invoice
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(
            @PathVariable Long id
    ) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}
