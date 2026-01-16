package com.codewith.RabiaLinkApp.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {
    private Long totalOrders;
    private Long ordersInProgress;
    private Long ordersCompleted;
    
    private BigDecimal totalRevenue;
    private BigDecimal totalCostOfGoods;
    private BigDecimal grossProfit;
    private Double profitMargin;
    
    private BigDecimal totalReceivables;
    private BigDecimal totalPayables;
    private BigDecimal netCashPosition;
    
    private BigDecimal totalPartnerCapital;
    private Long totalActivePartners;
    
    private Long overdueDaysCount;           // Count of invoices past due
    private BigDecimal overdueMoney;         // Total money overdue
    
    private List<TopClientDTO> topClients;
    private List<AgingReceivableDTO> criticalReceivables;
}
