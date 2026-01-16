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
public class OrdersPipelineReportDTO {
    private String orderStatus;
    private Long totalOrders;
    private BigDecimal totalValue;
    private Double percentageOfTotal;
}
