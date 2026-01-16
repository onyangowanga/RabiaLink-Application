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
public class PendingDeliveryDTO {
    private Long orderId;
    private String orderNumber;
    private Long clientId;
    private String clientName;
    private BigDecimal orderAmount;
    private String productName;
    private Long quantity;
    private LocalDateTime orderDate;
    private Long daysOverdue;
    private String expectedDeliveryDate;
}
