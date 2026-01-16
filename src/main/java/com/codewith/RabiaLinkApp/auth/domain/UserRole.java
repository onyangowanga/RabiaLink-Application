package com.codewith.RabiaLinkApp.auth.domain;

public enum UserRole {
    ADMIN,           // Full access
    MANAGER,         // Orders, invoices, payments, reports
    STAFF,           // View-only + create orders
    PARTNER          // Partner-specific operations
}
