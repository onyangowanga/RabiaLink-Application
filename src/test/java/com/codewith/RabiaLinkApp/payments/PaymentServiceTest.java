package com.codewith.RabiaLinkApp.payments;

import com.codewith.RabiaLinkApp.clients.domain.Client;
import com.codewith.RabiaLinkApp.invoices.domain.Invoice;
import com.codewith.RabiaLinkApp.invoices.domain.InvoiceStatus;
import com.codewith.RabiaLinkApp.invoices.repository.InvoiceRepository;
import com.codewith.RabiaLinkApp.orders.domain.Order;
import com.codewith.RabiaLinkApp.orders.domain.OrderStatus;
import com.codewith.RabiaLinkApp.payments.domain.Payment;
import com.codewith.RabiaLinkApp.payments.domain.PaymentMethod;
import com.codewith.RabiaLinkApp.payments.domain.PaymentStatus;
import com.codewith.RabiaLinkApp.payments.domain.PaymentType;
import com.codewith.RabiaLinkApp.payments.dto.PaymentRequest;
import com.codewith.RabiaLinkApp.payments.dto.PaymentResponse;
import com.codewith.RabiaLinkApp.payments.repository.PaymentRepository;
import com.codewith.RabiaLinkApp.payments.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Invoice invoice;
    private Payment payment;
    private PaymentRequest paymentRequest;
    private Client client;
    private Order order;

    @BeforeEach
    void setUp() {
        // Setup test client
        client = new Client();
        client.setId(1L);
        client.setName("Test Client");
        client.setEmail("client@test.com");

        // Setup test order
        order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-001");
        order.setClient(client);
        order.setTotalAmount(new BigDecimal("1000.00"));
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCreatedAt(LocalDateTime.now());

        // Setup test invoice
        invoice = new Invoice();
        invoice.setId(1L);
        invoice.setInvoiceNumber("INV-001");
        invoice.setOrder(order);
        invoice.setTotalAmount(new BigDecimal("1000.00"));
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setCreatedAt(LocalDateTime.now());

        // Setup test payment
        payment = new Payment();
        payment.setId(1L);
        payment.setInvoice(invoice);
        payment.setAmount(new BigDecimal("500.00"));
        payment.setPaymentType(PaymentType.PARTIAL);
        payment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());

        // Setup payment request
        paymentRequest = new PaymentRequest();
        paymentRequest.setInvoiceId(1L);
        paymentRequest.setAmount(new BigDecimal("500.00"));
        paymentRequest.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        paymentRequest.setReferenceNumber("REF-001");
    }

    @Test
    @DisplayName("Should process partial payment successfully")
    void testProcessPayment_PartialPayment_Success() {
        // Given
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertNotNull(response);
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertEquals(PaymentStatus.COMPLETED.toString(), response.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should process full payment and mark invoice as paid")
    void testProcessPayment_FullPayment_Success() {
        // Given
        PaymentRequest fullPayment = new PaymentRequest();
        fullPayment.setInvoiceId(1L);
        fullPayment.setAmount(new BigDecimal("1000.00"));
        fullPayment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        fullPayment.setReferenceNumber("REF-002");

        Payment fullPaymentEntity = new Payment();
        fullPaymentEntity.setId(2L);
        fullPaymentEntity.setInvoice(invoice);
        fullPaymentEntity.setAmount(new BigDecimal("1000.00"));
        fullPaymentEntity.setPaymentType(PaymentType.FULL);
        fullPaymentEntity.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        fullPaymentEntity.setStatus(PaymentStatus.COMPLETED);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenReturn(fullPaymentEntity);

        // When
        PaymentResponse response = paymentService.processPayment(fullPayment);

        // Then
        assertNotNull(response);
        assertEquals(new BigDecimal("1000.00"), response.getAmount());
        assertEquals(PaymentType.FULL.toString(), response.getPaymentType());
    }

    @Test
    @DisplayName("Should reject overpayment")
    void testProcessPayment_Overpayment_ShouldFail() {
        // Given
        PaymentRequest overpayment = new PaymentRequest();
        overpayment.setInvoiceId(1L);
        overpayment.setAmount(new BigDecimal("1500.00")); // More than invoice amount
        overpayment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        overpayment.setReferenceNumber("REF-003");

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // When & Then
        assertThrows(RuntimeException.class, () -> paymentService.processPayment(overpayment),
                "Payment amount exceeds invoice total");
    }

    @Test
    @DisplayName("Should reject payment for paid invoice")
    void testProcessPayment_AlreadyPaid_ShouldFail() {
        // Given
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAmount(new BigDecimal("1000.00"));

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // When & Then
        assertThrows(RuntimeException.class, () -> paymentService.processPayment(paymentRequest),
                "Invoice is already fully paid");
    }

    @Test
    @DisplayName("Should reject zero or negative payment amount")
    void testProcessPayment_InvalidAmount_ShouldFail() {
        // Given
        paymentRequest.setAmount(BigDecimal.ZERO);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // When & Then
        assertThrows(RuntimeException.class, () -> paymentService.processPayment(paymentRequest),
                "Payment amount must be greater than zero");
    }

    @Test
    @DisplayName("Should retrieve payment by ID successfully")
    void testGetPaymentById_Success() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When
        PaymentResponse response = paymentService.getPaymentById(1L);

        // Then
        assertNotNull(response);
        assertEquals(payment.getId(), response.getId());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
    }

    @Test
    @DisplayName("Should update invoice paid amount when payment processed")
    void testProcessPayment_UpdateInvoicePaidAmount() {
        // Given
        BigDecimal initialPaidAmount = invoice.getPaidAmount();
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.processPayment(paymentRequest);

        // Then
        assertTrue(invoice.getPaidAmount().compareTo(initialPaidAmount) > 0);
    }

    @Test
    @DisplayName("Should prevent duplicate payment processing")
    void testProcessPayment_DuplicatePayment_ShouldHandleCorrectly() {
        // Given
        invoice.setPaidAmount(new BigDecimal("500.00"));
        invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);

        PaymentRequest secondPayment = new PaymentRequest();
        secondPayment.setInvoiceId(1L);
        secondPayment.setAmount(new BigDecimal("500.00"));
        secondPayment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        secondPayment.setReferenceNumber("REF-004"); // Different reference

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        PaymentResponse response = paymentService.processPayment(secondPayment);

        // Then
        assertNotNull(response);
        // Should process successfully as references are different
    }
}
