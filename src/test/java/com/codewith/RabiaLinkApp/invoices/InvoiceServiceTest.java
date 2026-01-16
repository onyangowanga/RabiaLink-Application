package com.codewith.RabiaLinkApp.invoices;

import com.codewith.RabiaLinkApp.clients.domain.Client;
import com.codewith.RabiaLinkApp.invoices.domain.Invoice;
import com.codewith.RabiaLinkApp.invoices.domain.InvoiceStatus;
import com.codewith.RabiaLinkApp.invoices.dto.InvoiceResponse;
import com.codewith.RabiaLinkApp.invoices.repository.InvoiceRepository;
import com.codewith.RabiaLinkApp.invoices.service.InvoiceService;
import com.codewith.RabiaLinkApp.invoices.service.impl.InvoiceServiceImpl;
import com.codewith.RabiaLinkApp.orders.domain.Order;
import com.codewith.RabiaLinkApp.orders.domain.OrderStatus;
import com.codewith.RabiaLinkApp.orders.repository.OrderRepository;
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
@DisplayName("Invoice Service Tests")
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Order order;
    private Invoice invoice;
    private Client client;

    @BeforeEach
    void setUp() {
        // Setup test client
        client = new Client();
        client.setId(1L);
        client.setName("Test Client");
        client.setEmail("client@test.com");
        client.setPhone("1234567890");

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
        invoice.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create invoice for confirmed order successfully")
    void testCreateInvoiceForOrder_Success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        // When
        InvoiceResponse response = invoiceService.createInvoiceForOrder(1L);

        // Then
        assertNotNull(response);
        assertEquals("INV-001", response.getInvoiceNumber());
        assertEquals(new BigDecimal("1000.00"), response.getTotalAmount());
        assertEquals(InvoiceStatus.UNPAID.toString(), response.getStatus());
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void testCreateInvoiceForOrder_OrderNotFound() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> invoiceService.createInvoiceForOrder(999L));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve invoice by ID successfully")
    void testGetInvoiceById_Success() {
        // Given
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // When
        InvoiceResponse response = invoiceService.getInvoiceById(1L);

        // Then
        assertNotNull(response);
        assertEquals(invoice.getId(), response.getId());
        assertEquals("INV-001", response.getInvoiceNumber());
    }

    @Test
    @DisplayName("Should return null when invoice not found")
    void testGetInvoiceById_NotFound() {
        // Given
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        InvoiceResponse response = invoiceService.getInvoiceById(999L);

        // Then
        assertNull(response);
    }

    @Test
    @DisplayName("Should update invoice status successfully")
    void testUpdateInvoiceStatus_Success() {
        // Given
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        // When
        InvoiceResponse response = invoiceService.updateInvoiceStatus(1L, InvoiceStatus.PARTIALLY_PAID);

        // Then
        assertNotNull(response);
        assertEquals(InvoiceStatus.PARTIALLY_PAID.toString(), response.getStatus());
    }

    @Test
    @DisplayName("Should prevent invoice deletion when paid")
    void testDeleteInvoice_WhenPaid_ShouldFail() {
        // Given
        invoice.setStatus(InvoiceStatus.PAID);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // When & Then
        assertThrows(RuntimeException.class, () -> invoiceService.deleteInvoice(1L));
        verify(invoiceRepository, never()).deleteById(1L);
    }

    @Test
    @DisplayName("Should allow invoice deletion when unpaid")
    void testDeleteInvoice_WhenUnpaid_Success() {
        // Given
        invoice.setStatus(InvoiceStatus.UNPAID);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        // When
        invoiceService.deleteInvoice(1L);

        // Then
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }
}
