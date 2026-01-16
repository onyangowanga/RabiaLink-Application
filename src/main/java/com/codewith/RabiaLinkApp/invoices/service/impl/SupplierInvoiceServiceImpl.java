package com.codewith.RabiaLinkApp.invoices.service.impl;

import com.codewith.RabiaLinkApp.common.exception.ResourceNotFoundException;
import com.codewith.RabiaLinkApp.invoices.domain.SupplierInvoice;
import com.codewith.RabiaLinkApp.invoices.domain.SupplierInvoiceItem;
import com.codewith.RabiaLinkApp.invoices.dto.SupplierInvoiceRequest;
import com.codewith.RabiaLinkApp.invoices.dto.SupplierInvoiceResponse;
import com.codewith.RabiaLinkApp.invoices.repository.SupplierInvoiceRepository;
import com.codewith.RabiaLinkApp.invoices.service.SupplierInvoiceService;
import com.codewith.RabiaLinkApp.invoices.service.InvoiceService;
import com.codewith.RabiaLinkApp.orders.repository.OrderRepository;
import com.codewith.RabiaLinkApp.products.repository.ProductRepository;
import com.codewith.RabiaLinkApp.clients.repository.ClientRepository;
import com.codewith.RabiaLinkApp.products.domain.Product;
import com.codewith.RabiaLinkApp.orders.domain.Order;
import com.codewith.RabiaLinkApp.orders.domain.OrderItem;
import com.codewith.RabiaLinkApp.orders.domain.OrderStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupplierInvoiceServiceImpl implements SupplierInvoiceService {

    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final InvoiceService invoiceService;

    public SupplierInvoiceServiceImpl(
            SupplierInvoiceRepository supplierInvoiceRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository,
            ClientRepository clientRepository,
            InvoiceService invoiceService
    ) {
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.clientRepository = clientRepository;
        this.invoiceService = invoiceService;
    }

    @Override
    public SupplierInvoiceResponse createSupplierInvoice(SupplierInvoiceRequest request) {
        Order order = null;

        // Case 1: Order exists - use existing order
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

            // Ensure order is CONFIRMED if provided
            if (!OrderStatus.CONFIRMED.equals(order.getStatus())) {
                throw new IllegalStateException("Order must be CONFIRMED before accepting supplier invoice and generating client invoice");
            }
        }
        // Case 2: No order - auto-generate order from supplier invoice (verbal order)
        else if (request.getClientId() != null) {
            order = createOrderFromSupplierInvoice(request);
        } else {
            throw new IllegalStateException("Either orderId or clientId must be provided");
        }

        // Idempotency: if supplier invoice with same number exists, return it
        SupplierInvoice existing = supplierInvoiceRepository.findBySupplierInvoiceNumber(request.getSupplierInvoiceNumber());
        if (existing != null) {
            SupplierInvoiceResponse resp = new SupplierInvoiceResponse();
            resp.setId(existing.getId());
            resp.setSupplierName(existing.getSupplierName());
            resp.setSupplierInvoiceNumber(existing.getSupplierInvoiceNumber());
            resp.setInvoiceDate(existing.getInvoiceDate());
            return resp;
        }

        SupplierInvoice si = new SupplierInvoice();
        si.setOrder(order);
        si.setSupplierName(request.getSupplierName());
        si.setSupplierInvoiceNumber(request.getSupplierInvoiceNumber());
        si.setInvoiceDate(Optional.ofNullable(request.getInvoiceDate()).orElse(LocalDateTime.now()));
        si.setCreatedBy(request.getCreatedBy());

        List<SupplierInvoiceItem> items = new ArrayList<>();
        if (request.getItems() != null) {
            for (SupplierInvoiceRequest.SupplierInvoiceItemRequest ireq : request.getItems()) {
                SupplierInvoiceItem item = new SupplierInvoiceItem();
                Product p = productRepository.findById(ireq.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + ireq.getProductId()));

                // Validate delivered quantity does not exceed ordered quantity (only if order already existed)
                if (request.getOrderId() != null) {
                    int delivered = ireq.getQuantity() == null ? 0 : ireq.getQuantity();
                    Integer orderedQty = findOrderedQuantity(order, p.getId());
                    if (orderedQty == null) {
                        throw new IllegalStateException("Product id " + p.getId() + " is not part of order " + order.getId());
                    }
                    if (delivered > orderedQty) {
                        throw new IllegalStateException("Delivered quantity (" + delivered + ") exceeds ordered quantity (" + orderedQty + ") for product " + p.getId());
                    }
                }

                item.setSupplierInvoice(si);
                item.setProduct(p);
                item.setQuantity(ireq.getQuantity());
                item.setUnitPrice(ireq.getUnitPrice());
                BigDecimal line = ireq.getUnitPrice().multiply(new BigDecimal(ireq.getQuantity()));
                item.setLineTotal(line);
                items.add(item);
            }
        }

        si.setItems(items);
        SupplierInvoice saved = supplierInvoiceRepository.save(si);

        // Trigger creation of client invoice only if order was pre-existing and CONFIRMED
        // For auto-generated orders (verbal), wait for client confirmation before generating client invoice
        if (request.getOrderId() != null) {
            List<com.codewith.RabiaLinkApp.invoices.domain.Invoice> existingClientInvoices = invoiceService.getInvoicesByOrderId(order.getId()).stream()
                    .map(r -> {
                        com.codewith.RabiaLinkApp.invoices.domain.Invoice inv = new com.codewith.RabiaLinkApp.invoices.domain.Invoice();
                        inv.setId(r.getId());
                        return inv;
                    }).toList();
            if (existingClientInvoices.isEmpty()) {
                invoiceService.createInvoiceFromSupplierInvoice(saved.getId());
            }
        }

        // Build response
        SupplierInvoiceResponse resp = new SupplierInvoiceResponse();
        resp.setId(saved.getId());
        resp.setSupplierName(saved.getSupplierName());
        resp.setSupplierInvoiceNumber(saved.getSupplierInvoiceNumber());
        resp.setInvoiceDate(saved.getInvoiceDate());

        return resp;
    }

    /**
     * Auto-generate an Order from supplier invoice data (for verbal orders)
     * Order is created with CREATED status, awaiting client confirmation
     */
    private Order createOrderFromSupplierInvoice(SupplierInvoiceRequest request) {
        // Fetch client
        com.codewith.RabiaLinkApp.clients.domain.Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        // Create order with unique order number based on supplier invoice number + timestamp
        Order order = new Order();
        order.setOrderNumber("AUTO-" + request.getSupplierInvoiceNumber() + "-" + System.currentTimeMillis());
        order.setClient(client);
        order.setStatus(OrderStatus.CREATED);  // Client must confirm this order
        order.setCreatedAt(LocalDateTime.now());

        // Create order items from supplier invoice items
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (SupplierInvoiceRequest.SupplierInvoiceItemRequest ireq : request.getItems()) {
                Product p = productRepository.findById(ireq.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + ireq.getProductId()));

                OrderItem oi = new OrderItem();
                oi.setOrder(order);
                oi.setProduct(p);
                oi.setQuantity(ireq.getQuantity());
                oi.setUnitPrice(ireq.getUnitPrice());
                BigDecimal lineTotal = ireq.getUnitPrice().multiply(new BigDecimal(ireq.getQuantity()));
                oi.setLineTotal(lineTotal);

                orderItems.add(oi);
                totalAmount = totalAmount.add(lineTotal);
            }
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        // Save the auto-generated order
        return orderRepository.save(order);
    }

    private Integer findOrderedQuantity(Order order, Long productId) {
        if (order.getItems() == null) return null;
        for (OrderItem oi : order.getItems()) {
            if (oi.getProduct() != null && oi.getProduct().getId() != null && oi.getProduct().getId().equals(productId)) {
                return oi.getQuantity();
            }
        }
        return null;
    }
}

