package com.codewith.RabiaLinkApp.clients.repository;

import com.codewith.RabiaLinkApp.clients.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
