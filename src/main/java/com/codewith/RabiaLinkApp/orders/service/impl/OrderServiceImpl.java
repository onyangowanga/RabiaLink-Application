package com.codewith.RabiaLinkApp.orders.service.impl;

import com.codewith.RabiaLinkApp.clients.domain.Client;
import com.codewith.RabiaLinkApp.clients.repository.ClientRepository;
import com.codewith.RabiaLinkApp.orders.domain.Order;
import com.codewith.RabiaLinkApp.orders.domain.OrderStatus;
import com.codewith.RabiaLinkApp.orders.service.OrderService;
import com.codewith.RabiaLinkApp.orders.domain.OrderItem;
import com.codewith.RabiaLinkApp.orders.dto.OrderItemRequest;
import com.codewith.RabiaLinkApp.orders.dto.OrderRequest;
import com.codewith.RabiaLinkApp.orders.dto.OrderResponse;
import com.codewith.RabiaLinkApp.orders.repository.OrderRepository;
import com.codewith.RabiaLinkApp.products.domain.Product;
import com.codewith.RabiaLinkApp.products.repository.ProductRepository;
import com.codewith.RabiaLinkApp.common.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            ClientRepository clientRepository,
            ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
    }

    @Override
    public OrderResponse createOrder(OrderRequest request) {

        // 1. Validate Client
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Client not found with id: " + request.getClientId())
                );

        // 2. Create Order (Parent)
        Order order = new Order();
        order.setClient(client);
        order.setDeliverySite(request.getDeliverySite());
        order.setStatus(OrderStatus.CREATED);
        order.setOrderNumber("ORD-" + System.currentTimeMillis());

        List<OrderItem> orderItems = new ArrayList<>();

        // 3. Create Order Items (Children)
        for (OrderItemRequest itemRequest : request.getItems()) {

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Product not found with id: " + itemRequest.getProductId()
                            )
                    );

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());

            orderItems.add(item);
        }

        order.setItems(orderItems);

        // 4. Persist Entire Graph in One Transaction
        Order savedOrder = orderRepository.save(order);

        // 5. Map to Response DTO
        return OrderResponse.from(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with id: " + orderId)
                );

        return OrderResponse.from(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderResponse::from)
                .toList();
    }
}
