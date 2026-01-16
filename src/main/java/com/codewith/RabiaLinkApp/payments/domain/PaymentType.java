package com.codewith.RabiaLinkApp.payments.domain;

public enum PaymentType {
    CLIENT_PAYMENT,        // Client paying for client invoice
    SUPPLIER_PAYMENT,      // Rabiya paying supplier invoice
    PARTNER_CONTRIBUTION   // Partner contribution to shared invoice
}
