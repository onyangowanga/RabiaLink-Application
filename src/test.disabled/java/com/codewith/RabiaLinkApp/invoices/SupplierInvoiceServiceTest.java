package com.codewith.RabiaLinkApp.invoices;

import com.codewith.RabiaLinkApp.clients.domain.Client;
import com.codewith.RabiaLinkApp.clients.repository.ClientRepository;
import com.codewith.RabiaLinkApp.invoices.domain.Invoice;
import com.codewith.RabiaLinkApp.invoices.domain.InvoiceStatus;
import com.codewith.RabiaLinkApp.invoices.domain.SupplierInvoice;
import com.codewith.RabiaLinkApp.invoices.dto.SupplierInvoiceRequest;
import com.codewith.RabiaLinkApp.invoices.dto.SupplierInvoiceResponse;
import com.codewith.RabiaLinkApp.invoices.repository.InvoiceRepository;
import com.codewith.RabiaLinkApp.invoices.repository.SupplierInvoiceRepository;
import com.codewith.RabiaLinkApp.invoices.service.InvoiceService;
import com.codewith.RabiaLinkApp.invoices.service.SupplierInvoiceService;
import com.codewith.RabiaLinkApp.orders.domain.Order;
import com.codewith.RabiaLinkApp.orders.domain.OrderItem;
import com.codewith.RabiaLinkApp.orders.domain.OrderStatus;
import com.codewith.RabiaLinkApp.orders.repository.OrderRepository;
import com.codewith.RabiaLinkApp.products.domain.Product;
import com.codewith.RabiaLinkApp.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SupplierInvoiceServiceTest {

    @Autowired
    private SupplierInvoiceService supplierInvoiceService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private SupplierInvoiceRepository supplierInvoiceRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Client client;
    private Order order;
    private Product product1, product2;

    @BeforeEach
    void setUp() {
        // Create a client
        client = new Client();
        client.setName("Acme Construction Ltd");
        client.setEmail("contact@acme.co.ke");
        client.setStatus("ACTIVE");
        client = clientRepository.save(client);

        // Create products with default markups (use unique names with timestamp)
        long ts = System.currentTimeMillis();
        product1 = new Product("River Sand " + ts, "TON", new BigDecimal("0.15")); // 15% markup
        product1 = productRepository.save(product1);

        product2 = new Product("Ballast " + ts, "TON", new BigDecimal("0.20")); // 20% markup
        product2 = productRepository.save(product2);

        // Create an order in CONFIRMED status
        order = new Order();
        order.setClient(client);
        order.setOrderNumber("ORD-" + ts);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setDeliverySite("Site A");

        // Add order items
        OrderItem item1 = new OrderItem();
        item1.setOrder(order);
        item1.setProduct(product1);
        item1.setQuantity(10);
        item1.setUnitPrice(new BigDecimal("1000"));
        item1.setLineTotal(new BigDecimal("10000"));

        OrderItem item2 = new OrderItem();
        item2.setOrder(order);
        item2.setProduct(product2);
        item2.setQuantity(5);
        item2.setUnitPrice(new BigDecimal("1200"));
        item2.setLineTotal(new BigDecimal("6000"));

        order.setItems(List.of(item1, item2));
        order = orderRepository.save(order);
        orderRepository.flush();
    }

    @Test
    void testSupplierInvoiceCreation() {
        SupplierInvoiceRequest req = new SupplierInvoiceRequest();
        req.setOrderId(order.getId());
        req.setSupplierName("Supplier Ltd");
        req.setSupplierInvoiceNumber("SUP-2026-001");
        req.setInvoiceDate(LocalDateTime.now());
        req.setCreatedBy("john.doe");

        SupplierInvoiceRequest.SupplierInvoiceItemRequest item1 = new SupplierInvoiceRequest.SupplierInvoiceItemRequest();
        item1.setProductId(product1.getId());
        item1.setQuantity(10);
        item1.setUnitPrice(new BigDecimal("900"));

        SupplierInvoiceRequest.SupplierInvoiceItemRequest item2 = new SupplierInvoiceRequest.SupplierInvoiceItemRequest();
        item2.setProductId(product2.getId());
        item2.setQuantity(5);
        item2.setUnitPrice(new BigDecimal("1000"));

        req.setItems(List.of(item1, item2));

        SupplierInvoiceResponse resp = supplierInvoiceService.createSupplierInvoice(req);

        assertNotNull(resp);
        assertNotNull(resp.getId());
        assertEquals("Supplier Ltd", resp.getSupplierName());
        assertEquals("SUP-2026-001", resp.getSupplierInvoiceNumber());
    }

    @Test
    void testIdempotency() {
        SupplierInvoiceRequest req = new SupplierInvoiceRequest();
        req.setOrderId(order.getId());
        req.setSupplierName("Supplier Ltd");
        req.setSupplierInvoiceNumber("SUP-2026-002");
        req.setCreatedBy("john.doe");
        req.setItems(List.of());

        // Create first time
        SupplierInvoiceResponse resp1 = supplierInvoiceService.createSupplierInvoice(req);
        assertNotNull(resp1.getId());
        long firstId = resp1.getId();

        // Create second time with same invoice number
        SupplierInvoiceResponse resp2 = supplierInvoiceService.createSupplierInvoice(req);
        assertNotNull(resp2.getId());
        assertEquals(firstId, resp2.getId());

        // Verify only one supplier invoice exists
        List<SupplierInvoice> all = supplierInvoiceRepository.findByOrderId(order.getId());
        assertEquals(1, all.size());
    }

    @Test
    void testDeliveredQuantityExceedsOrderedQuantity() {
        SupplierInvoiceRequest req = new SupplierInvoiceRequest();
        req.setOrderId(order.getId());
        req.setSupplierName("Supplier Ltd");
        req.setSupplierInvoiceNumber("SUP-2026-003");
        req.setCreatedBy("john.doe");

        SupplierInvoiceRequest.SupplierInvoiceItemRequest item1 = new SupplierInvoiceRequest.SupplierInvoiceItemRequest();
        item1.setProductId(product1.getId());
        item1.setQuantity(15); // Exceeds ordered 10
        item1.setUnitPrice(new BigDecimal("900"));

        req.setItems(List.of(item1));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            supplierInvoiceService.createSupplierInvoice(req);
        });
        assertTrue(ex.getMessage().contains("exceeds ordered quantity"));
    }

    @Test
    void testClientInvoiceGenerationWithMarkup() {
        SupplierInvoiceRequest req = new SupplierInvoiceRequest();
        req.setOrderId(order.getId());
        req.setSupplierName("Supplier Ltd");
        req.setSupplierInvoiceNumber("SUP-2026-004");
        req.setInvoiceDate(LocalDateTime.now());
        req.setCreatedBy("john.doe");

        SupplierInvoiceRequest.SupplierInvoiceItemRequest item1 = new SupplierInvoiceRequest.SupplierInvoiceItemRequest();
        item1.setProductId(product1.getId());
        item1.setQuantity(10);
        item1.setUnitPrice(new BigDecimal("1000")); // Supplier unit price

        req.setItems(List.of(item1));

        supplierInvoiceService.createSupplierInvoice(req);

        // Verify client invoice was created
        List<Invoice> invoices = invoiceRepository.findByOrderId(order.getId());
        assertEquals(1, invoices.size());

        Invoice clientInvoice = invoices.get(0);
        assertNotNull(clientInvoice.getId());
        assertEquals(InvoiceStatus.DRAFT, clientInvoice.getStatus());

        // Verify markup calculation
        // Supplier price: 1000, Product markup: 15% (0.15)
        // Expected client price: 1000 * (1 + 0.15) = 1150
        BigDecimal expectedUnitPrice = new BigDecimal("1000").multiply(BigDecimal.ONE.add(new BigDecimal("0.15")));
        assertEquals(expectedUnitPrice, clientInvoice.getItems().get(0).getUnitPrice());

        // Verify audit fields
        assertEquals("john.doe", clientInvoice.getCreatedBy());
    }

    @Test
    void testOrderMustBeConfirmed() {
        Order unconfirmedOrder = new Order();
        unconfirmedOrder.setClient(client);
        unconfirmedOrder.setOrderNumber("ORD-UNCONFIRMED-" + System.currentTimeMillis());
        unconfirmedOrder.setStatus(OrderStatus.CREATED);
        unconfirmedOrder = orderRepository.save(unconfirmedOrder);

        SupplierInvoiceRequest req = new SupplierInvoiceRequest();
        req.setOrderId(unconfirmedOrder.getId());
        req.setSupplierName("Supplier Ltd");
        req.setSupplierInvoiceNumber("SUP-2026-005");
        req.setCreatedBy("john.doe");
        req.setItems(List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            supplierInvoiceService.createSupplierInvoice(req);
        });
        assertTrue(ex.getMessage().contains("CONFIRMED"));
    }
}
