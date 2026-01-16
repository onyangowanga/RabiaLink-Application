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
public class TopClientDTO {
    private Long clientId;
    private String clientName;
    private String clientPhone;
    private String clientEmail;
    private Long totalOrders;
    private BigDecimal totalOrderValue;
    private BigDecimal totalPaid;
    private BigDecimal totalOutstanding;
    private BigDecimal averageOrderValue;
    private Double paymentCompliancePercentage;
}
