package com.codewith.RabiaLinkApp.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfitLossReportDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalRevenue;           // Sum of all invoice totals
    private BigDecimal totalCostOfGoods;       // Sum of all supplier invoice totals
    private BigDecimal grossProfit;            // Revenue - COGS
    private Double grossProfitMargin;          // (Gross Profit / Revenue) * 100
    private BigDecimal operatingExpenses;      // Partner allocations
    private BigDecimal netProfit;              // Gross Profit - Operating Expenses
    private Double netProfitMargin;            // (Net Profit / Revenue) * 100
    private Long totalOrders;
    private Long totalInvoicesPaid;
    private Long totalInvoicesPartiallyPaid;
    private Long totalInvoicesOutstanding;
}
