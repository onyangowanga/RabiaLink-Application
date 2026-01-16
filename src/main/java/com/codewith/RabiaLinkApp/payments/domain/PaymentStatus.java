package com.codewith.RabiaLinkApp.payments.domain;

public enum PaymentStatus {
    PENDING,     // Payment initiated but not confirmed
    COMPLETED,   // Payment successfully processed
    FAILED,      // Payment failed
    REVERSED     // Payment was reversed/refunded
}
