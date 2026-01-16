package com.codewith.RabiaLinkApp.reports.controller;

import com.codewith.RabiaLinkApp.reports.dto.*;
import com.codewith.RabiaLinkApp.reports.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // ================== OPERATIONAL REPORTS ==================

    /**
     * Get orders grouped by status
     */
    @GetMapping("/operations/orders-pipeline")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<List<OrdersPipelineReportDTO>> getOrdersPipeline() {
        return ResponseEntity.ok(reportService.getOrdersPipeline());
    }

    /**
     * Get pending deliveries
     */
    @GetMapping("/operations/pending-deliveries")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<List<PendingDeliveryDTO>> getPendingDeliveries() {
        return ResponseEntity.ok(reportService.getPendingDeliveries());
    }

    /**
     * Get aging receivables
     */
    @GetMapping("/operations/aging-receivables")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<List<AgingReceivableDTO>> getAgingReceivables() {
        return ResponseEntity.ok(reportService.getAgingReceivables());
    }

    /**
     * Get top clients
     * @param limit number of top clients to retrieve (default: 10)
     */
    @GetMapping("/operations/top-clients")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<List<TopClientDTO>> getTopClients(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportService.getTopClients(limit));
    }

    // ================== FINANCIAL REPORTS ==================

    /**
     * Get Profit & Loss report for date range
     */
    @GetMapping("/financial/profit-loss")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<ProfitLossReportDTO> getProfitLossReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getProfitLossReport(startDate, endDate));
    }

    /**
     * Get profit breakdown per order
     */
    @GetMapping("/financial/profit-per-order")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<List<ProfitPerOrderDTO>> getProfitPerOrder() {
        return ResponseEntity.ok(reportService.getProfitPerOrder());
    }

    /**
     * Get outstanding invoices from clients
     */
    @GetMapping("/financial/outstanding-invoices")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<List<OutstandingTransactionDTO>> getOutstandingInvoices() {
        return ResponseEntity.ok(reportService.getOutstandingInvoices());
    }

    // ================== PARTNER REPORTS ==================

    /**
     * Get partner capital contributions
     */
    @GetMapping("/partners/capital-contributions")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<List<PartnerCapitalContributionDTO>> getPartnerCapitalContributions() {
        return ResponseEntity.ok(reportService.getPartnerCapitalContributions());
    }

    /**
     * Get partner profit distributions
     */
    @GetMapping("/partners/profit-distributions")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<List<PartnerProfitDistributionDTO>> getPartnerProfitDistributions() {
        return ResponseEntity.ok(reportService.getPartnerProfitDistributions());
    }

    // ================== DASHBOARD ==================

    /**
     * Get comprehensive dashboard summary
     */
    @GetMapping("/dashboard/summary")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
        return ResponseEntity.ok(reportService.getDashboardSummary());
    }
}
