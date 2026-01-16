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
public class AgingReceivableDTO {
    private Long invoiceId;
    private String invoiceNumber;
    private Long clientId;
    private String clientName;
    private BigDecimal invoiceAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private Long daysPastDue;
    private String ageCategory; // "0-30 days", "31-60 days", "61-90 days", "90+ days"
    private String status;
}
