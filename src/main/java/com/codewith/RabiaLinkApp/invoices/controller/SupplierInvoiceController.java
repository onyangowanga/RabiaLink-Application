package com.codewith.RabiaLinkApp.invoices.controller;

import com.codewith.RabiaLinkApp.invoices.domain.SupplierInvoice;
import com.codewith.RabiaLinkApp.invoices.dto.SupplierInvoiceRequest;
import com.codewith.RabiaLinkApp.invoices.dto.SupplierInvoiceResponse;
import com.codewith.RabiaLinkApp.invoices.repository.SupplierInvoiceRepository;
import com.codewith.RabiaLinkApp.invoices.service.SupplierInvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier-invoices")
public class SupplierInvoiceController {

    private final SupplierInvoiceService supplierInvoiceService;
    private final SupplierInvoiceRepository supplierInvoiceRepository;

    public SupplierInvoiceController(
            SupplierInvoiceService supplierInvoiceService,
            SupplierInvoiceRepository supplierInvoiceRepository
    ) {
        this.supplierInvoiceService = supplierInvoiceService;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
    }

    /**
     * POST /api/supplier-invoices
     * Create a new supplier invoice (with or without order)
     */
    @PostMapping
    public ResponseEntity<SupplierInvoiceResponse> create(@Valid @RequestBody SupplierInvoiceRequest request) {
        SupplierInvoiceResponse resp = supplierInvoiceService.createSupplierInvoice(request);
        return ResponseEntity.ok(resp);
    }

    /**
     * GET /api/supplier-invoices
     * Retrieve all supplier invoices
     */
    @GetMapping
    public ResponseEntity<List<SupplierInvoice>> getAll() {
        List<SupplierInvoice> invoices = supplierInvoiceRepository.findAll();
        return ResponseEntity.ok(invoices);
    }

    /**
     * GET /api/supplier-invoices/{id}
     * Retrieve a specific supplier invoice by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SupplierInvoice> getById(@PathVariable Long id) {
        return supplierInvoiceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/supplier-invoices/by-order/{orderId}
     * Retrieve all supplier invoices for a specific order
     */
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<SupplierInvoice>> getByOrderId(@PathVariable Long orderId) {
        List<SupplierInvoice> invoices = supplierInvoiceRepository.findByOrderId(orderId);
        return ResponseEntity.ok(invoices);
    }
}
