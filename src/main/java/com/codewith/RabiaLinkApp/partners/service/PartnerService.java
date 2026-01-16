package com.codewith.RabiaLinkApp.partners.service;

import com.codewith.RabiaLinkApp.partners.dto.PartnerRequest;
import com.codewith.RabiaLinkApp.partners.dto.PartnerResponse;
import com.codewith.RabiaLinkApp.partners.dto.PartnerTransactionRequest;
import com.codewith.RabiaLinkApp.partners.dto.PartnerTransactionResponse;
import java.math.BigDecimal;
import java.util.List;

public interface PartnerService {

    /**
     * Register a new partner
     */
    PartnerResponse registerPartner(PartnerRequest request);

    /**
     * Get partner by ID
     */
    PartnerResponse getPartnerById(Long partnerId);

    /**
     * Get all partners
     */
    List<PartnerResponse> getAllPartners();

    /**
     * Record a capital contribution for a partner
     */
    PartnerTransactionResponse recordCapitalContribution(PartnerTransactionRequest request);

    /**
     * Allocate profit to partners based on their share percentage
     * Calculates: (Total Invoice Amount - Supplier Cost) * (Partner % / 100)
     */
    void allocateProfitToPartners(Long invoiceId, BigDecimal supplierCost);

    /**
     * Get transaction history for a partner
     */
    List<PartnerTransactionResponse> getPartnerTransactions(Long partnerId);

    /**
     * Get current capital balance for a partner
     */
    BigDecimal getPartnerCapitalBalance(Long partnerId);

    /**
     * Get total realized profit for a partner
     */
    BigDecimal getPartnerTotalProfit(Long partnerId);

    /**
     * Get partner reporting summary
     */
    PartnerReportResponse getPartnerReport(Long partnerId);
}
