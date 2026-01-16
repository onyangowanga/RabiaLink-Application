package com.codewith.RabiaLinkApp.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfitPerOrderDTO {
    private Long orderId;
    private String orderNumber;
    private Long clientId;
    private String clientName;
    private Long supplierId;
    private String supplierName;
    private BigDecimal clientInvoiceAmount;    // What client pays
    private BigDecimal supplierInvoiceAmount;  // What we pay supplier
    private BigDecimal grossProfit;            // Client Amount - Supplier Amount
    private Double profitMarginPercentage;     // (Gross Profit / Client Amount) * 100
    private BigDecimal partnerAllocation;      // Profit allocated to partners
    private BigDecimal netProfit;              // Gross Profit - Partner Allocation
    private Double netProfitMarginPercentage;  // (Net Profit / Client Amount) * 100
    private LocalDateTime orderDate;
    private String orderStatus;
}
