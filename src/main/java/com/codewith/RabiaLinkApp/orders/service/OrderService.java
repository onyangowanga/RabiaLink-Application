package com.codewith.RabiaLinkApp.orders.service;

import com.codewith.RabiaLinkApp.orders.dto.OrderRequest;
import com.codewith.RabiaLinkApp.orders.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long orderId);

    List<OrderResponse> getAllOrders();
}
