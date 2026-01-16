package com.codewith.RabiaLinkApp.invoices.service.impl;

import com.codewith.RabiaLinkApp.common.exception.ResourceNotFoundException;
import com.codewith.RabiaLinkApp.invoices.domain.Invoice;
import com.codewith.RabiaLinkApp.invoices.domain.InvoiceItem;
import com.codewith.RabiaLinkApp.invoices.domain.InvoiceStatus;
import com.codewith.RabiaLinkApp.invoices.domain.SupplierInvoice;
import com.codewith.RabiaLinkApp.invoices.domain.SupplierInvoiceItem;
import com.codewith.RabiaLinkApp.invoices.repository.SupplierInvoiceRepository;
import com.codewith.RabiaLinkApp.invoices.dto.InvoiceResponse;
import com.codewith.RabiaLinkApp.invoices.repository.InvoiceRepository;
import com.codewith.RabiaLinkApp.invoices.service.InvoiceService;
import com.codewith.RabiaLinkApp.orders.domain.Order;
import com.codewith.RabiaLinkApp.orders.domain.OrderItem;
import com.codewith.RabiaLinkApp.orders.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing invoices with strict immutability rules.
 *
 * Business Rules:
 * 1. Invoices are snapshots - Once created, item prices and quantities are frozen
 * 2. Invoice lifecycle: DRAFT → ISSUED → PAID/OVERDUE/CANCELLED
 * 3. DRAFT invoices can be modified
 * 4. ISSUED invoices and their items are immutable
 * 5. InvoiceItems can only be deleted if parent Invoice is DRAFT
 * 6. All financial amounts use BigDecimal with precision constraints
 *
 * Note: Immutability is enforced at the service layer, not at the JPA level.
 */
@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;

    public InvoiceServiceImpl(
            InvoiceRepository invoiceRepository,
            OrderRepository orderRepository,
            SupplierInvoiceRepository supplierInvoiceRepository
    ) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
    }

    @Override
    public InvoiceResponse createInvoice(Long orderId) {
        // 1. Validate Order exists
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with id: " + orderId)
                );

        // 2. Check if invoice already exists for this order (1:1 relationship)
        List<Invoice> existingInvoices = invoiceRepository.findByOrderId(orderId);
        if (!existingInvoices.isEmpty()) {
            throw new IllegalStateException("Invoice already exists for order id: " + orderId);
        }

        // 3. Create Invoice (snapshot container)
        Invoice invoice = new Invoice();
        invoice.setClient(order.getClient());
        invoice.setOrder(order);
        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setDueDate(LocalDateTime.now().plusDays(30)); // 30 days payment term
        // NOTE: issuedAt is NOT set here - it will be set explicitly when status changes to ISSUED

        // 4. Create Invoice Items (frozen snapshots from Order Items)
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem orderItem : order.getItems()) {
                InvoiceItem invoiceItem = new InvoiceItem();
                invoiceItem.setInvoice(invoice);
                
                // SNAPSHOT: Freeze product name at creation time
                invoiceItem.setProductName(orderItem.getProduct().getName());
                
                // SNAPSHOT: Freeze quantities and prices at creation time
                invoiceItem.setQuantity(orderItem.getQuantity());
                invoiceItem.setUnitPrice(orderItem.getUnitPrice());

                // Calculate and freeze line total
                BigDecimal lineTotal = invoiceItem.getUnitPrice()
                        .multiply(new BigDecimal(invoiceItem.getQuantity()));
                invoiceItem.setLineTotal(lineTotal);

                subtotal = subtotal.add(lineTotal);
                invoiceItems.add(invoiceItem);
            }
        } else {
            throw new IllegalStateException("Cannot create invoice for order with no items");
        }

        invoice.setItems(invoiceItems);
        
        // Calculate financial breakdown
        invoice.setSubtotal(subtotal);
        BigDecimal tax = calculateTax(subtotal); // TODO: Implement tax calculation logic
        invoice.setTax(tax);
        invoice.setTotalAmount(subtotal.add(tax));

        // 5. Persist Invoice with frozen Items in single transaction
        Invoice savedInvoice = invoiceRepository.save(invoice);

        return InvoiceResponse.from(savedInvoice);
    }

    /**
     * Calculate tax based on subtotal.
     * TODO: Implement actual tax calculation logic based on business rules.
     */
    private BigDecimal calculateTax(BigDecimal subtotal) {
        // Placeholder: 0% tax
        return BigDecimal.ZERO;
    }

    @Override
    public InvoiceResponse createInvoiceFromSupplierInvoice(Long supplierInvoiceId) {
        SupplierInvoice si = supplierInvoiceRepository.findById(supplierInvoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierInvoice not found: " + supplierInvoiceId));

        Order order = si.getOrder();
        if (order == null) {
            throw new IllegalStateException("Supplier invoice must reference an order to generate client invoice");
        }

        // Ensure no existing invoice for order
        List<Invoice> existing = invoiceRepository.findByOrderId(order.getId());
        if (!existing.isEmpty()) {
            throw new IllegalStateException("Invoice already exists for order id: " + order.getId());
        }

        Invoice invoice = new Invoice();
        invoice.setClient(order.getClient());
        invoice.setOrder(order);
        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setInvoiceDate(si.getInvoiceDate() != null ? si.getInvoiceDate() : LocalDateTime.now());
        invoice.setDueDate(invoice.getInvoiceDate().plusDays(30));
        invoice.setCreatedBy(si.getCreatedBy() != null ? si.getCreatedBy() : si.getSupplierName());

        List<InvoiceItem> invoiceItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        if (si.getItems() != null && !si.getItems().isEmpty()) {
            for (SupplierInvoiceItem sItem : si.getItems()) {
                InvoiceItem ii = new InvoiceItem();
                ii.setInvoice(invoice);
                ii.setProductName(sItem.getProduct().getName());

                Integer qty = sItem.getQuantity();
                ii.setQuantity(qty);

                // Apply product default markup if present (assumed as decimal fraction, e.g., 0.10 for 10%)
                BigDecimal markup = sItem.getProduct().getDefaultMarkup();
                if (markup == null) markup = BigDecimal.ZERO;

                BigDecimal supplierUnit = sItem.getUnitPrice();
                BigDecimal clientUnitPrice = supplierUnit.multiply(BigDecimal.ONE.add(markup));
                ii.setUnitPrice(clientUnitPrice);

                BigDecimal lineTotal = clientUnitPrice.multiply(new BigDecimal(qty));
                ii.setLineTotal(lineTotal);

                subtotal = subtotal.add(lineTotal);
                invoiceItems.add(ii);
            }
        } else {
            throw new IllegalStateException("Supplier invoice contains no items");
        }

        invoice.setItems(invoiceItems);
        invoice.setSubtotal(subtotal);
        BigDecimal tax = calculateTax(subtotal);
        invoice.setTax(tax);
        invoice.setTotalAmount(subtotal.add(tax));

        Invoice saved = invoiceRepository.save(invoice);

        return InvoiceResponse.from(saved);
    }

    @Override
    public InvoiceResponse getInvoiceById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invoice not found with id: " + invoiceId)
                );

        return InvoiceResponse.from(invoice);
    }

    @Override
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @Override
    public List<InvoiceResponse> getInvoicesByClientId(Long clientId) {
        return invoiceRepository.findByClientId(clientId)
                .stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @Override
    public List<InvoiceResponse> getInvoicesByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @Override
    public InvoiceResponse updateInvoiceStatus(Long invoiceId, String status) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invoice not found with id: " + invoiceId)
                );

        // Business Rule: Only DRAFT invoices can be modified
        if (!invoice.getStatus().equals(InvoiceStatus.DRAFT)) {
            throw new IllegalStateException(
                "Cannot update status. Only DRAFT invoices can be modified. Current status: " + invoice.getStatus()
            );
        }

        try {
            InvoiceStatus newStatus = InvoiceStatus.valueOf(status.toUpperCase());
            invoice.setStatus(newStatus);

            // Set issuedAt timestamp explicitly when status changes to ISSUED
            if (newStatus.equals(InvoiceStatus.ISSUED)) {
                invoice.setIssuedAt(LocalDateTime.now());
            }

            Invoice updatedInvoice = invoiceRepository.save(invoice);
            return InvoiceResponse.from(updatedInvoice);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid invoice status: " + status);
        }
    }

    @Override
    public void deleteInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invoice not found with id: " + invoiceId)
                );

        invoiceRepository.delete(invoice);
    }
}
