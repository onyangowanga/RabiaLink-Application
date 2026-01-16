package com.codewith.RabiaLinkApp.payments.service.impl;

import com.codewith.RabiaLinkApp.common.exception.ResourceNotFoundException;
import com.codewith.RabiaLinkApp.invoices.domain.Invoice;
import com.codewith.RabiaLinkApp.invoices.domain.InvoiceStatus;
import com.codewith.RabiaLinkApp.invoices.domain.SupplierInvoice;
import com.codewith.RabiaLinkApp.invoices.repository.InvoiceRepository;
import com.codewith.RabiaLinkApp.invoices.repository.SupplierInvoiceRepository;
import com.codewith.RabiaLinkApp.payments.domain.Payment;
import com.codewith.RabiaLinkApp.payments.domain.PaymentStatus;
import com.codewith.RabiaLinkApp.payments.domain.PaymentType;
import com.codewith.RabiaLinkApp.payments.dto.PaymentRequest;
import com.codewith.RabiaLinkApp.payments.dto.PaymentResponse;
import com.codewith.RabiaLinkApp.payments.repository.PaymentRepository;
import com.codewith.RabiaLinkApp.payments.service.PaymentService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository,
            SupplierInvoiceRepository supplierInvoiceRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        // Validate payment type and associated invoice
        Invoice clientInvoice = null;
        SupplierInvoice supplierInvoice = null;
        BigDecimal invoiceAmount = BigDecimal.ZERO;

        if (PaymentType.CLIENT_PAYMENT.equals(request.getPaymentType())) {
            if (request.getInvoiceId() == null) {
                throw new IllegalArgumentException("Invoice ID is required for CLIENT_PAYMENT");
            }

            clientInvoice = invoiceRepository.findById(request.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + request.getInvoiceId()));

            invoiceAmount = clientInvoice.getTotalAmount();

            // Validate no overpayment
            BigDecimal alreadyPaid = getTotalPaidForInvoice(request.getInvoiceId());
            BigDecimal totalAfterPayment = alreadyPaid.add(request.getAmount());

            if (totalAfterPayment.compareTo(invoiceAmount) > 0) {
                throw new IllegalStateException("Payment amount would exceed invoice total. " +
                        "Invoice: " + invoiceAmount + ", Already Paid: " + alreadyPaid + ", Attempting to pay: " + request.getAmount());
            }
        }
        else if (PaymentType.SUPPLIER_PAYMENT.equals(request.getPaymentType())) {
            if (request.getSupplierInvoiceId() == null) {
                throw new IllegalArgumentException("Supplier Invoice ID is required for SUPPLIER_PAYMENT");
            }

            supplierInvoice = supplierInvoiceRepository.findById(request.getSupplierInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier Invoice not found with id: " + request.getSupplierInvoiceId()));

            // Calculate supplier invoice total from items
            invoiceAmount = supplierInvoice.getItems().stream()
                    .map(item -> item.getLineTotal() != null ? item.getLineTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Validate no overpayment
            BigDecimal alreadyPaid = getTotalPaidForSupplierInvoice(request.getSupplierInvoiceId());
            BigDecimal totalAfterPayment = alreadyPaid.add(request.getAmount());

            if (totalAfterPayment.compareTo(invoiceAmount) > 0) {
                throw new IllegalStateException("Payment amount would exceed supplier invoice total. " +
                        "Invoice: " + invoiceAmount + ", Already Paid: " + alreadyPaid + ", Attempting to pay: " + request.getAmount());
            }
        }
        else if (PaymentType.PARTNER_CONTRIBUTION.equals(request.getPaymentType())) {
            // Partner contribution validation - flexible, just validate amount is positive
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Partner contribution amount must be greater than 0");
            }
        }

        // Create and save payment
        Payment payment = new Payment();
        payment.setPaymentType(request.getPaymentType());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(request.getAmount());
        payment.setInvoice(clientInvoice);
        payment.setSupplierInvoice(supplierInvoice);
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now());
        payment.setStatus(PaymentStatus.COMPLETED);  // Assume payment is processed successfully
        payment.setTransactionReference(request.getTransactionReference());
        payment.setNotes(request.getNotes());
        payment.setCreatedBy(request.getCreatedBy());

        Payment savedPayment = paymentRepository.save(payment);

        // Update invoice status based on payment
        if (clientInvoice != null) {
            updateInvoiceStatus(clientInvoice);
        }

        return mapToResponse(savedPayment);
    }

    /**
     * Update invoice status to PAID or PARTIALLY_PAID based on payments
     */
    private void updateInvoiceStatus(Invoice invoice) {
        BigDecimal totalPaid = getTotalPaidForInvoice(invoice.getId());
        BigDecimal totalAmount = invoice.getTotalAmount();

        if (totalPaid.compareTo(totalAmount) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);
    }

    @Override
    public List<PaymentResponse> getPaymentsByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsBySupplierInvoice(Long supplierInvoiceId) {
        return paymentRepository.findBySupplierInvoiceId(supplierInvoiceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalPaidForInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceIdAndStatus(invoiceId, PaymentStatus.COMPLETED).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalPaidForSupplierInvoice(Long supplierInvoiceId) {
        return paymentRepository.findBySupplierInvoiceIdAndStatus(supplierInvoiceId, PaymentStatus.COMPLETED).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        return mapToResponse(payment);
    }

    @Override
    public PaymentResponse reversePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (!PaymentStatus.COMPLETED.equals(payment.getStatus())) {
            throw new IllegalStateException("Only COMPLETED payments can be reversed");
        }

        payment.setStatus(PaymentStatus.REVERSED);
        Payment savedPayment = paymentRepository.save(payment);

        // Update invoice status after reversal
        if (payment.getInvoice() != null) {
            updateInvoiceStatus(payment.getInvoice());
        }

        return mapToResponse(savedPayment);
    }

    /**
     * Map Payment entity to PaymentResponse DTO
     */
    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentType(payment.getPaymentType());
        response.setStatus(payment.getStatus());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setAmount(payment.getAmount());
        response.setInvoiceId(payment.getInvoice() != null ? payment.getInvoice().getId() : null);
        response.setSupplierInvoiceId(payment.getSupplierInvoice() != null ? payment.getSupplierInvoice().getId() : null);
        response.setPaymentDate(payment.getPaymentDate());
        response.setCreatedAt(payment.getCreatedAt());
        response.setTransactionReference(payment.getTransactionReference());
        response.setNotes(payment.getNotes());
        return response;
    }
}
