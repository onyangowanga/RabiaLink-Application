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
public class OutstandingTransactionDTO {
    private Long id;
    private String transactionNumber;        // Invoice number
    private String transactionType;          // "CLIENT_INVOICE" or "SUPPLIER_INVOICE"
    private Long partyId;                    // Client ID or Supplier ID
    private String partyName;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private Long daysPastDue;
    private String status;
    private String priority;                 // "HIGH", "MEDIUM", "LOW" based on days overdue
}
