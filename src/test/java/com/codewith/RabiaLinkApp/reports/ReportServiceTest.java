package com.codewith.RabiaLinkApp.reports;

import com.codewith.RabiaLinkApp.clients.domain.Client;
import com.codewith.RabiaLinkApp.invoices.domain.Invoice;
import com.codewith.RabiaLinkApp.invoices.domain.InvoiceStatus;
import com.codewith.RabiaLinkApp.invoices.repository.InvoiceRepository;
import com.codewith.RabiaLinkApp.invoices.domain.SupplierInvoice;
import com.codewith.RabiaLinkApp.invoices.repository.SupplierInvoiceRepository;
import com.codewith.RabiaLinkApp.orders.domain.Order;
import com.codewith.RabiaLinkApp.orders.domain.OrderStatus;
import com.codewith.RabiaLinkApp.orders.repository.OrderRepository;
import com.codewith.RabiaLinkApp.partners.domain.Partner;
import com.codewith.RabiaLinkApp.partners.repository.PartnerRepository;
import com.codewith.RabiaLinkApp.payments.repository.PaymentRepository;
import com.codewith.RabiaLinkApp.reports.dto.*;
import com.codewith.RabiaLinkApp.reports.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Report Service Tests")
class ReportServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private SupplierInvoiceRepository supplierInvoiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PartnerRepository partnerRepository;

    @InjectMocks
    private ReportService reportService;

    private List<Order> orders;
    private List<Invoice> invoices;
    private List<Partner> partners;

    @BeforeEach
    void setUp() {
        // Setup test data
        Client client = new Client();
        client.setId(1L);
        client.setName("Test Client");
        client.setEmail("client@test.com");

        orders = new ArrayList<>();
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-001");
        order.setClient(client);
        order.setTotalAmount(new BigDecimal("1000.00"));
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCreatedAt(LocalDateTime.now());
        orders.add(order);

        invoices = new ArrayList<>();
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setInvoiceNumber("INV-001");
        invoice.setOrder(order);
        invoice.setTotalAmount(new BigDecimal("1000.00"));
        invoice.setPaidAmount(new BigDecimal("500.00"));
        invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setDueDate(LocalDate.now().minusDays(5));
        invoices.add(invoice);

        partners = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId(1L);
        partner.setPartnerCode("P-001");
        partner.setPartnerName("Test Partner");
        partner.setTotalCapital(new BigDecimal("50000.00"));
        partner.setTotalProfitEarned(new BigDecimal("10000.00"));
        partner.setProfitSharePercentage(new BigDecimal("20.00"));
        partners.add(partner);
    }

    @Test
    @DisplayName("Should generate orders pipeline report")
    void testGetOrdersPipeline_Success() {
        // Given
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<OrdersPipelineReportDTO> results = reportService.getOrdersPipeline();

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should generate pending deliveries report")
    void testGetPendingDeliveries_Success() {
        // Given
        when(orderRepository.findByStatus(OrderStatus.CONFIRMED)).thenReturn(orders);

        // When
        List<PendingDeliveryDTO> results = reportService.getPendingDeliveries();

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    @DisplayName("Should generate aging receivables report")
    void testGetAgingReceivables_Success() {
        // Given
        when(invoiceRepository.findByStatusIn(
            new InvoiceStatus[]{InvoiceStatus.UNPAID, InvoiceStatus.PARTIALLY_PAID}))
            .thenReturn(invoices);

        // When
        List<AgingReceivableDTO> results = reportService.getAgingReceivables();

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    @DisplayName("Should identify overdue invoices in aging report")
    void testGetAgingReceivables_IdentifiesOverdue() {
        // Given
        Invoice overdueInvoice = new Invoice();
        overdueInvoice.setId(2L);
        overdueInvoice.setInvoiceNumber("INV-002");
        overdueInvoice.setTotalAmount(new BigDecimal("500.00"));
        overdueInvoice.setPaidAmount(BigDecimal.ZERO);
        overdueInvoice.setStatus(InvoiceStatus.UNPAID);
        overdueInvoice.setDueDate(LocalDate.now().minusDays(30)); // 30 days overdue
        invoices.add(overdueInvoice);

        when(invoiceRepository.findByStatusIn(
            new InvoiceStatus[]{InvoiceStatus.UNPAID, InvoiceStatus.PARTIALLY_PAID}))
            .thenReturn(invoices);

        // When
        List<AgingReceivableDTO> results = reportService.getAgingReceivables();

        // Then
        assertNotNull(results);
        boolean hasOverdue = results.stream()
            .anyMatch(r -> r.getDaysPastDue() > 0);
        assertTrue(hasOverdue);
    }

    @Test
    @DisplayName("Should generate top clients report with correct ranking")
    void testGetTopClients_Success() {
        // Given
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<TopClientDTO> results = reportService.getTopClients(10);

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
        // Should be ordered by total order value descending
        if (results.size() > 1) {
            assertTrue(results.get(0).getTotalOrderValue()
                .compareTo(results.get(1).getTotalOrderValue()) >= 0);
        }
    }

    @Test
    @DisplayName("Should respect limit parameter in top clients report")
    void testGetTopClients_RespectLimit() {
        // Given
        when(orderRepository.findAll()).thenReturn(orders);
        int limit = 5;

        // When
        List<TopClientDTO> results = reportService.getTopClients(limit);

        // Then
        assertNotNull(results);
        assertTrue(results.size() <= limit);
    }

    @Test
    @DisplayName("Should generate profit and loss report")
    void testGetProfitLossReport_Success() {
        // Given
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        when(invoiceRepository.findAll()).thenReturn(invoices);
        when(supplierInvoiceRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        ProfitLossReportDTO result = reportService.getProfitLossReport(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
        assertTrue(result.getTotalRevenue().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    @DisplayName("Should calculate correct gross profit margin")
    void testGetProfitLossReport_CorrectProfitMargin() {
        // Given
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        when(invoiceRepository.findAll()).thenReturn(invoices);
        when(supplierInvoiceRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        ProfitLossReportDTO result = reportService.getProfitLossReport(startDate, endDate);

        // Then
        assertNotNull(result);
        if (result.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0) {
            assertNotNull(result.getGrossProfitMargin());
            assertTrue(result.getGrossProfitMargin().compareTo(new BigDecimal("-100")) >= 0);
            assertTrue(result.getGrossProfitMargin().compareTo(new BigDecimal("100")) <= 0);
        }
    }

    @Test
    @DisplayName("Should generate partner capital contributions report")
    void testGetPartnerCapitalContributions_Success() {
        // Given
        when(partnerRepository.findAll()).thenReturn(partners);

        // When
        List<PartnerCapitalContributionDTO> results = reportService.getPartnerCapitalContributions();

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertEquals(partners.get(0).getPartnerCode(), results.get(0).getPartnerCode());
    }

    @Test
    @DisplayName("Should generate partner profit distributions report")
    void testGetPartnerProfitDistributions_Success() {
        // Given
        when(partnerRepository.findAll()).thenReturn(partners);

        // When
        List<PartnerProfitDistributionDTO> results = reportService.getPartnerProfitDistributions();

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertEquals(new BigDecimal("10000.00"), results.get(0).getTotalProfitEarned());
    }

    @Test
    @DisplayName("Should generate dashboard summary with all metrics")
    void testGetDashboardSummary_Success() {
        // Given
        when(orderRepository.findAll()).thenReturn(orders);
        when(invoiceRepository.findAll()).thenReturn(invoices);
        when(partnerRepository.findAll()).thenReturn(partners);

        // When
        DashboardSummaryDTO summary = reportService.getDashboardSummary();

        // Then
        assertNotNull(summary);
        assertTrue(summary.getTotalOrders() >= 0);
        assertTrue(summary.getTotalRevenue().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(summary.getTotalReceivables().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(summary.getTotalActivePartners() >= 0);
    }

    @Test
    @DisplayName("Should handle empty data in reports")
    void testReports_WithEmptyData() {
        // Given
        when(orderRepository.findAll()).thenReturn(new ArrayList<>());
        when(invoiceRepository.findAll()).thenReturn(new ArrayList<>());
        when(partnerRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        List<OrdersPipelineReportDTO> pipeline = reportService.getOrdersPipeline();
        DashboardSummaryDTO summary = reportService.getDashboardSummary();

        // Then
        assertNotNull(pipeline);
        assertNotNull(summary);
        assertEquals(0, pipeline.size());
        assertEquals(0, summary.getTotalOrders());
    }

    @Test
    @DisplayName("Should aggregate correctly in dashboard summary")
    void testGetDashboardSummary_CorrectAggregation() {
        // Given
        when(orderRepository.findAll()).thenReturn(orders);
        when(invoiceRepository.findAll()).thenReturn(invoices);
        when(partnerRepository.findAll()).thenReturn(partners);

        // When
        DashboardSummaryDTO summary = reportService.getDashboardSummary();

        // Then
        assertNotNull(summary);
        // Total revenue should match unpaid invoice amounts
        assertEquals(1, summary.getTotalOrders());
        assertTrue(summary.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(summary.getTotalReceivables().compareTo(BigDecimal.ZERO) > 0);
    }
}
