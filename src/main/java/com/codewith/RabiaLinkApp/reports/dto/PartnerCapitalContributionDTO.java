package com.codewith.RabiaLinkApp.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerCapitalContributionDTO {
    private Long partnerId;
    private String partnerName;
    private String partnerCode;
    private BigDecimal totalCapitalContributed;
    private BigDecimal profitSharePercentage;
    private Long numberOfContributions;
    private String status;
}
