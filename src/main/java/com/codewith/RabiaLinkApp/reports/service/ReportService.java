package com.codewith.RabiaLinkApp.reports.service;

import com.codewith.RabiaLinkApp.invoices.domain.Invoice;
import com.codewith.RabiaLinkApp.invoices.domain.InvoiceStatus;
import com.codewith.RabiaLinkApp.invoices.repository.InvoiceRepository;
import com.codewith.RabiaLinkApp.orders.domain.Order;
import com.codewith.RabiaLinkApp.orders.domain.OrderStatus;
import com.codewith.RabiaLinkApp.orders.repository.OrderRepository;
import com.codewith.RabiaLinkApp.payments.domain.Payment;
import com.codewith.RabiaLinkApp.payments.domain.PaymentStatus;
import com.codewith.RabiaLinkApp.payments.repository.PaymentRepository;
import com.codewith.RabiaLinkApp.partners.domain.Partner;
import com.codewith.RabiaLinkApp.partners.domain.PartnerTransaction;
import com.codewith.RabiaLinkApp.partners.repository.PartnerRepository;
import com.codewith.RabiaLinkApp.partners.repository.PartnerTransactionRepository;
import com.codewith.RabiaLinkApp.reports.dto.*;
import com.codewith.RabiaLinkApp.clients.domain.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private PartnerTransactionRepository partnerTransactionRepository;

    // ================== OPERATIONAL REPORTS ==================

    /**
     * Get orders grouped by status with counts and values
     */
    public List<OrdersPipelineReportDTO> getOrdersPipeline() {
        List<Order> allOrders = orderRepository.findAll();
        BigDecimal totalOrderValue = allOrders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Arrays.stream(OrderStatus.values())
                .map(status -> {
                    List<Order> ordersWithStatus = allOrders.stream()
                            .filter(o -> o.getStatus() == status)
                            .collect(Collectors.toList());

                    BigDecimal statusValue = ordersWithStatus.stream()
                            .map(Order::getTotalAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Double percentage = totalOrderValue.compareTo(BigDecimal.ZERO) > 0
                            ? statusValue.divide(totalOrderValue, 4, RoundingMode.HALF_UP)
                                    .multiply(new BigDecimal(100))
                                    .doubleValue()
                            : 0.0;

                    return OrdersPipelineReportDTO.builder()
                            .orderStatus(status.toString())
                            .totalOrders((long) ordersWithStatus.size())
                            .totalValue(statusValue)
                            .percentageOfTotal(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get pending deliveries - orders in progress
     */
    public List<PendingDeliveryDTO> getPendingDeliveries() {
        List<Order> pendingOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED)
                .collect(Collectors.toList());

        return pendingOrders.stream()
                .map(order -> {
                    Client client = order.getClient();
                    Long daysOverdue = calculateDaysSince(order.getCreatedAt());

                    return PendingDeliveryDTO.builder()
                            .orderId(order.getId())
                            .orderNumber(order.getOrderNumber())
                            .clientId(client != null ? client.getId() : null)
                            .clientName(client != null ? client.getName() : "N/A")
                            .orderAmount(order.getTotalAmount())
                            .productName("N/A")
                            .quantity(0L)
                            .orderDate(order.getCreatedAt())
                            .daysOverdue(daysOverdue)
                            .expectedDeliveryDate("TBD")
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get aging receivables - unpaid and partially paid invoices grouped by age
     */
    public List<AgingReceivableDTO> getAgingReceivables() {
        List<Invoice> receivables = invoiceRepository.findAll().stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PARTIALLY_PAID ||
                               inv.getStatus() == InvoiceStatus.ISSUED ||
                               inv.getStatus() == InvoiceStatus.OVERDUE)
                .collect(Collectors.toList());

        return receivables.stream()
                .map(invoice -> {
                    Client client = invoice.getClient();
                    Long daysPastDue = calculateDaysSince(invoice.getDueDate());
                    BigDecimal paidAmount = getTotalPaidForInvoice(invoice.getId());
                    BigDecimal outstanding = invoice.getTotalAmount() != null 
                            ? invoice.getTotalAmount().subtract(paidAmount)
                            : BigDecimal.ZERO;

                    return AgingReceivableDTO.builder()
                            .invoiceId(invoice.getId())
                            .invoiceNumber(invoice.getInvoiceNumber())
                            .clientId(client != null ? client.getId() : null)
                            .clientName(client != null ? client.getName() : "N/A")
                            .invoiceAmount(invoice.getTotalAmount())
                            .paidAmount(paidAmount)
                            .outstandingAmount(outstanding)
                            .issueDate(invoice.getCreatedAt())
                            .dueDate(invoice.getDueDate())
                            .daysPastDue(daysPastDue)
                            .ageCategory(getAgeCategory(daysPastDue))
                            .status(invoice.getStatus().toString())
                            .build();
                })
                .sorted(Comparator.comparing(AgingReceivableDTO::getDaysPastDue).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get top clients by order count and revenue
     */
    public List<TopClientDTO> getTopClients(int limit) {
        List<Invoice> allInvoices = invoiceRepository.findAll();

        Map<Long, List<Invoice>> invoicesByClient = allInvoices.stream()
                .filter(inv -> inv.getClient() != null)
                .collect(Collectors.groupingBy(inv -> inv.getClient().getId()));

        return invoicesByClient.entrySet().stream()
                .map(entry -> {
                    Long clientId = entry.getKey();
                    List<Invoice> clientInvoices = entry.getValue();
                    Client client = clientInvoices.stream()
                            .map(Invoice::getClient)
                            .findFirst()
                            .orElse(null);

                    BigDecimal totalOrderValue = clientInvoices.stream()
                            .map(Invoice::getTotalAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalPaid = clientInvoices.stream()
                            .map(inv -> getTotalPaidForInvoice(inv.getId()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal outstanding = totalOrderValue.subtract(totalPaid);

                    BigDecimal avgOrderValue = clientInvoices.size() > 0
                            ? totalOrderValue.divide(new BigDecimal(clientInvoices.size()), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    double paymentCompliance = totalOrderValue.compareTo(BigDecimal.ZERO) > 0
                            ? totalPaid.divide(totalOrderValue, 4, RoundingMode.HALF_UP)
                                    .multiply(new BigDecimal(100))
                                    .doubleValue()
                            : 0.0;

                    return TopClientDTO.builder()
                            .clientId(clientId)
                            .clientName(client != null ? client.getName() : "N/A")
                            .clientPhone(client != null ? client.getPhone() : "N/A")
                            .clientEmail(client != null ? client.getEmail() : "N/A")
                            .totalOrders((long) clientInvoices.size())
                            .totalOrderValue(totalOrderValue)
                            .totalPaid(totalPaid)
                            .totalOutstanding(outstanding)
                            .averageOrderValue(avgOrderValue)
                            .paymentCompliancePercentage(paymentCompliance)
                            .build();
                })
                .sorted(Comparator.comparing(TopClientDTO::getTotalOrderValue).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ================== FINANCIAL REPORTS ==================

    /**
     * Get comprehensive P&L report for a date range
     */
    public ProfitLossReportDTO getProfitLossReport(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoicesInPeriod = invoiceRepository.findAll().stream()
                .filter(inv -> isInDateRange(inv.getCreatedAt(), startDate, endDate))
                .collect(Collectors.toList());

        BigDecimal totalRevenue = invoicesInPeriod.stream()
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Order> ordersInPeriod = orderRepository.findAll().stream()
                .filter(ord -> isInDateRange(ord.getCreatedAt(), startDate, endDate))
                .collect(Collectors.toList());

        BigDecimal totalCOGS = ordersInPeriod.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossProfit = totalRevenue.subtract(totalCOGS);
        Double grossProfitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? grossProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100))
                        .doubleValue()
                : 0.0;

        BigDecimal operatingExpenses = partnerTransactionRepository.findAll().stream()
                .filter(pt -> isInDateRange(pt.getCreatedAt(), startDate, endDate))
                .map(PartnerTransaction::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = grossProfit.subtract(operatingExpenses);
        Double netProfitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100))
                        .doubleValue()
                : 0.0;

        Long paidCount = invoicesInPeriod.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
                .count();

        Long partiallyPaidCount = invoicesInPeriod.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PARTIALLY_PAID)
                .count();

        Long outstandingCount = invoicesInPeriod.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.ISSUED ||
                               inv.getStatus() == InvoiceStatus.OVERDUE)
                .count();

        return ProfitLossReportDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue)
                .totalCostOfGoods(totalCOGS)
                .grossProfit(grossProfit)
                .grossProfitMargin(grossProfitMargin)
                .operatingExpenses(operatingExpenses)
                .netProfit(netProfit)
                .netProfitMargin(netProfitMargin)
                .totalOrders((long) ordersInPeriod.size())
                .totalInvoicesPaid(paidCount)
                .totalInvoicesPartiallyPaid(partiallyPaidCount)
                .totalInvoicesOutstanding(outstandingCount)
                .build();
    }

    /**
     * Get profit breakdown per order
     */
    public List<ProfitPerOrderDTO> getProfitPerOrder() {
        return orderRepository.findAll().stream()
                .map(order -> {
                    Invoice clientInvoice = invoiceRepository.findByOrderId(order.getId())
                            .stream()
                            .findFirst()
                            .orElse(null);
                    BigDecimal clientAmount = clientInvoice != null ? clientInvoice.getTotalAmount() : BigDecimal.ZERO;
                    BigDecimal supplierAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
                    BigDecimal grossProfit = clientAmount.subtract(supplierAmount);

                    Double profitMargin = clientAmount.compareTo(BigDecimal.ZERO) > 0
                            ? grossProfit.divide(clientAmount, 4, RoundingMode.HALF_UP)
                                    .multiply(new BigDecimal(100))
                                    .doubleValue()
                            : 0.0;

                    BigDecimal partnerAllocation = clientInvoice != null 
                            ? partnerTransactionRepository.findByInvoiceId(clientInvoice.getId())
                                    .stream()
                                    .map(PartnerTransaction::getAmount)
                                    .filter(Objects::nonNull)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                            : BigDecimal.ZERO;

                    BigDecimal netProfit = grossProfit.subtract(partnerAllocation);
                    Double netProfitMargin = clientAmount.compareTo(BigDecimal.ZERO) > 0
                            ? netProfit.divide(clientAmount, 4, RoundingMode.HALF_UP)
                                    .multiply(new BigDecimal(100))
                                    .doubleValue()
                            : 0.0;

                    Client client = order.getClient();

                    return ProfitPerOrderDTO.builder()
                            .orderId(order.getId())
                            .orderNumber(order.getOrderNumber())
                            .clientId(client != null ? client.getId() : null)
                            .clientName(client != null ? client.getName() : "N/A")
                            .supplierId(0L)
                            .supplierName("Supplier")
                            .clientInvoiceAmount(clientAmount)
                            .supplierInvoiceAmount(supplierAmount)
                            .grossProfit(grossProfit)
                            .profitMarginPercentage(profitMargin)
                            .partnerAllocation(partnerAllocation)
                            .netProfit(netProfit)
                            .netProfitMarginPercentage(netProfitMargin)
                            .orderDate(order.getCreatedAt())
                            .orderStatus(order.getStatus().toString())
                            .build();
                })
                .sorted(Comparator.comparing(ProfitPerOrderDTO::getOrderDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get outstanding invoices from clients
     */
    public List<OutstandingTransactionDTO> getOutstandingInvoices() {
        return invoiceRepository.findAll().stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.ISSUED ||
                               inv.getStatus() == InvoiceStatus.PARTIALLY_PAID ||
                               inv.getStatus() == InvoiceStatus.OVERDUE)
                .map(invoice -> {
                    BigDecimal paidAmount = getTotalPaidForInvoice(invoice.getId());
                    BigDecimal outstanding = invoice.getTotalAmount() != null
                            ? invoice.getTotalAmount().subtract(paidAmount)
                            : BigDecimal.ZERO;
                    Long daysPastDue = calculateDaysSince(invoice.getDueDate());

                    Client client = invoice.getClient();

                    return OutstandingTransactionDTO.builder()
                            .id(invoice.getId())
                            .transactionNumber(invoice.getInvoiceNumber())
                            .transactionType("CLIENT_INVOICE")
                            .partyId(client != null ? client.getId() : null)
                            .partyName(client != null ? client.getName() : "N/A")
                            .totalAmount(invoice.getTotalAmount())
                            .paidAmount(paidAmount)
                            .outstandingAmount(outstanding)
                            .issueDate(invoice.getCreatedAt())
                            .dueDate(invoice.getDueDate())
                            .daysPastDue(daysPastDue)
                            .status(invoice.getStatus().toString())
                            .priority(determinePriority(daysPastDue))
                            .build();
                })
                .sorted(Comparator.comparing(OutstandingTransactionDTO::getDaysPastDue).reversed())
                .collect(Collectors.toList());
    }

    // ================== PARTNER REPORTS ==================

    /**
     * Get partner capital contributions
     */
    public List<PartnerCapitalContributionDTO> getPartnerCapitalContributions() {
        return partnerRepository.findAll().stream()
                .map(partner -> {
                    Long contributionCount = partnerTransactionRepository.findByPartnerId(partner.getId())
                            .stream()
                            .filter(pt -> pt.getTransactionType().toString().equals("CAPITAL_CONTRIBUTION"))
                            .count();

                    return PartnerCapitalContributionDTO.builder()
                            .partnerId(partner.getId())
                            .partnerName(partner.getPartnerName())
                            .partnerCode(partner.getPartnerCode())
                            .totalCapitalContributed(partner.getTotalCapitalContributed())
                            .profitSharePercentage(partner.getProfitSharePercentage())
                            .numberOfContributions(contributionCount)
                            .status(partner.getStatus().toString())
                            .build();
                })
                .sorted(Comparator.comparing(PartnerCapitalContributionDTO::getTotalCapitalContributed).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get partner profit distributions
     */
    public List<PartnerProfitDistributionDTO> getPartnerProfitDistributions() {
        return partnerRepository.findAll().stream()
                .map(partner -> {
                    List<PartnerTransaction> transactions = partnerTransactionRepository.findByPartnerId(partner.getId());

                    BigDecimal totalDividendsPaid = transactions.stream()
                            .filter(pt -> pt.getTransactionType().toString().equals("DIVIDEND_PAYOUT"))
                            .map(PartnerTransaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    LocalDateTime lastAllocationDate = transactions.stream()
                            .filter(pt -> pt.getTransactionType().toString().equals("PROFIT_ALLOCATION"))
                            .map(PartnerTransaction::getCreatedAt)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);

                    List<PartnerProfitDistributionDTO.PartnerTransactionDetailDTO> transactionDetails = transactions.stream()
                            .map(pt -> PartnerProfitDistributionDTO.PartnerTransactionDetailDTO.builder()
                                    .transactionId(pt.getId())
                                    .transactionType(pt.getTransactionType().toString())
                                    .amount(pt.getAmount())
                                    .invoiceId(pt.getInvoiceId())
                                    .transactionDate(pt.getCreatedAt())
                                    .build())
                            .collect(Collectors.toList());

                    return PartnerProfitDistributionDTO.builder()
                            .partnerId(partner.getId())
                            .partnerName(partner.getPartnerName())
                            .partnerCode(partner.getPartnerCode())
                            .totalCapitalContributed(partner.getTotalCapitalContributed())
                            .profitSharePercentage(partner.getProfitSharePercentage())
                            .totalProfitEarned(partner.getTotalProfitEarned())
                            .totalDividendsPaid(totalDividendsPaid)
                            .pendingProfitAllocation(partner.getTotalProfitEarned().subtract(totalDividendsPaid))
                            .lastAllocationDate(lastAllocationDate)
                            .transactions(transactionDetails)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get comprehensive dashboard summary
     */
    public DashboardSummaryDTO getDashboardSummary() {
        List<Order> allOrders = orderRepository.findAll();
        List<Invoice> allInvoices = invoiceRepository.findAll();
        List<Partner> allPartners = partnerRepository.findAll();

        Long totalOrders = (long) allOrders.size();
        Long ordersInProgress = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED)
                .count();
        Long ordersCompleted = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count();

        BigDecimal totalRevenue = allInvoices.stream()
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCOGS = allOrders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossProfit = totalRevenue.subtract(totalCOGS);
        Double profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? grossProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100))
                        .doubleValue()
                : 0.0;

        BigDecimal totalReceivables = allInvoices.stream()
                .filter(inv -> inv.getStatus() != InvoiceStatus.PAID)
                .map(inv -> inv.getTotalAmount() != null 
                        ? inv.getTotalAmount().subtract(getTotalPaidForInvoice(inv.getId()))
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPayables = BigDecimal.ZERO;

        BigDecimal totalPartnerCapital = allPartners.stream()
                .map(Partner::getTotalCapitalContributed)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long activePartners = allPartners.stream()
                .filter(p -> p.getStatus().toString().equals("ACTIVE"))
                .count();

        List<AgingReceivableDTO> agingReceivables = getAgingReceivables();
        Long overdueDaysCount = agingReceivables.stream()
                .filter(ar -> ar.getDaysPastDue() > 0)
                .count();

        BigDecimal overdueMoney = agingReceivables.stream()
                .filter(ar -> ar.getDaysPastDue() > 0)
                .map(AgingReceivableDTO::getOutstandingAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TopClientDTO> topClients = getTopClients(5);
        List<AgingReceivableDTO> criticalReceivables = agingReceivables.stream()
                .filter(ar -> ar.getDaysPastDue() > 30)
                .limit(5)
                .collect(Collectors.toList());

        return DashboardSummaryDTO.builder()
                .totalOrders(totalOrders)
                .ordersInProgress(ordersInProgress)
                .ordersCompleted(ordersCompleted)
                .totalRevenue(totalRevenue)
                .totalCostOfGoods(totalCOGS)
                .grossProfit(grossProfit)
                .profitMargin(profitMargin)
                .totalReceivables(totalReceivables)
                .totalPayables(totalPayables)
                .netCashPosition(totalReceivables.subtract(totalPayables))
                .totalPartnerCapital(totalPartnerCapital)
                .totalActivePartners(activePartners)
                .overdueDaysCount(overdueDaysCount)
                .overdueMoney(overdueMoney)
                .topClients(topClients)
                .criticalReceivables(criticalReceivables)
                .build();
    }

    private BigDecimal getTotalPaidForInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long calculateDaysSince(LocalDateTime dateTime) {
        if (dateTime == null) return 0L;
        return java.time.temporal.ChronoUnit.DAYS.between(
                dateTime.toLocalDate(),
                LocalDate.now()
        );
    }

    private String getAgeCategory(Long daysPastDue) {
        if (daysPastDue <= 0) return "Current";
        if (daysPastDue <= 30) return "0-30 days";
        if (daysPastDue <= 60) return "31-60 days";
        if (daysPastDue <= 90) return "61-90 days";
        return "90+ days";
    }

    private String determinePriority(Long daysPastDue) {
        if (daysPastDue > 90) return "CRITICAL";
        if (daysPastDue > 60) return "HIGH";
        if (daysPastDue > 30) return "MEDIUM";
        return "LOW";
    }

    private boolean isInDateRange(LocalDateTime dateTime, LocalDate startDate, LocalDate endDate) {
        if (dateTime == null) return false;
        LocalDate date = dateTime.toLocalDate();
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
