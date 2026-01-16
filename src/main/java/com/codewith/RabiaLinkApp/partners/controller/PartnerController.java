package com.codewith.RabiaLinkApp.partners.controller;

import com.codewith.RabiaLinkApp.partners.dto.PartnerRequest;
import com.codewith.RabiaLinkApp.partners.dto.PartnerResponse;
import com.codewith.RabiaLinkApp.partners.dto.PartnerTransactionRequest;
import com.codewith.RabiaLinkApp.partners.dto.PartnerTransactionResponse;
import com.codewith.RabiaLinkApp.partners.service.PartnerReportResponse;
import com.codewith.RabiaLinkApp.partners.service.PartnerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    private final PartnerService partnerService;

    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    /**
     * POST /api/partners
     * Register a new partner
     */
    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<PartnerResponse> registerPartner(@Valid @RequestBody PartnerRequest request) {
        PartnerResponse response = partnerService.registerPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/partners/{id}
     * Get partner by ID
     */
    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_PARTNER"})
    public ResponseEntity<PartnerResponse> getPartner(@PathVariable Long id) {
        PartnerResponse response = partnerService.getPartnerById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/partners
     * Get all partners
     */
    @GetMapping
    public ResponseEntity<List<PartnerResponse>> getAllPartners() {
        List<PartnerResponse> responses = partnerService.getAllPartners();
        return ResponseEntity.ok(responses);
    }

    /**
     * POST /api/partners/{id}/capital-contribution
     * Record a capital contribution for a partner
     */
    @PostMapping("/{id}/capital-contribution")
    public ResponseEntity<PartnerTransactionResponse> recordCapitalContribution(
            @PathVariable Long id,
            @Valid @RequestBody PartnerTransactionRequest request) {
        
        request.setPartnerId(id);
        PartnerTransactionResponse response = partnerService.recordCapitalContribution(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/partners/{id}/transactions
     * Get transaction history for a partner
     */
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<PartnerTransactionResponse>> getPartnerTransactions(@PathVariable Long id) {
        List<PartnerTransactionResponse> responses = partnerService.getPartnerTransactions(id);
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/partners/{id}/capital-balance
     * Get current capital balance for a partner
     */
    @GetMapping("/{id}/capital-balance")
    public ResponseEntity<BigDecimal> getCapitalBalance(@PathVariable Long id) {
        BigDecimal balance = partnerService.getPartnerCapitalBalance(id);
        return ResponseEntity.ok(balance);
    }

    /**
     * GET /api/partners/{id}/total-profit
     * Get total realized profit for a partner
     */
    @GetMapping("/{id}/total-profit")
    public ResponseEntity<BigDecimal> getTotalProfit(@PathVariable Long id) {
        BigDecimal totalProfit = partnerService.getPartnerTotalProfit(id);
        return ResponseEntity.ok(totalProfit);
    }

    /**
     * GET /api/partners/{id}/report
     * Get comprehensive partner reporting summary
     */
    @GetMapping("/{id}/report")
    public ResponseEntity<PartnerReportResponse> getPartnerReport(@PathVariable Long id) {
        PartnerReportResponse report = partnerService.getPartnerReport(id);
        return ResponseEntity.ok(report);
    }
}
