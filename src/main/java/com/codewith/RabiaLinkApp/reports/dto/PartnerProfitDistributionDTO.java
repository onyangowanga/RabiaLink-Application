package com.codewith.RabiaLinkApp.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerProfitDistributionDTO {
    private Long partnerId;
    private String partnerName;
    private String partnerCode;
    private BigDecimal totalCapitalContributed;
    private BigDecimal profitSharePercentage;
    private BigDecimal totalProfitEarned;
    private BigDecimal totalDividendsPaid;
    private BigDecimal pendingProfitAllocation;
    private LocalDateTime lastAllocationDate;
    private List<PartnerTransactionDetailDTO> transactions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartnerTransactionDetailDTO {
        private Long transactionId;
        private String transactionType;
        private BigDecimal amount;
        private Long invoiceId;
        private LocalDateTime transactionDate;
    }
}
