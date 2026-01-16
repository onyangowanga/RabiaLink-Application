package com.codewith.RabiaLinkApp.payments.repository;

import com.codewith.RabiaLinkApp.payments.domain.Payment;
import com.codewith.RabiaLinkApp.payments.domain.PaymentStatus;
import com.codewith.RabiaLinkApp.payments.domain.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find all payments for a specific client invoice
    List<Payment> findByInvoiceId(Long invoiceId);
    
    // Find all payments for a specific supplier invoice
    List<Payment> findBySupplierInvoiceId(Long supplierInvoiceId);
    
    // Find payments by type
    List<Payment> findByPaymentType(PaymentType paymentType);
    
    // Find payments by status
    List<Payment> findByStatus(PaymentStatus status);
    
    // Find all completed payments for an invoice
    List<Payment> findByInvoiceIdAndStatus(Long invoiceId, PaymentStatus status);
    
    // Find all completed payments for a supplier invoice
    List<Payment> findBySupplierInvoiceIdAndStatus(Long supplierInvoiceId, PaymentStatus status);
    
    // Find payment by transaction reference
    Payment findByTransactionReference(String transactionReference);
}
