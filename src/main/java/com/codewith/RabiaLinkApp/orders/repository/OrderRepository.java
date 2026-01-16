package com.codewith.RabiaLinkApp.orders.repository;

import com.codewith.RabiaLinkApp.orders.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
