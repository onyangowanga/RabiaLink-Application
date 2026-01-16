package com.codewith.RabiaLinkApp.partners.repository;

import com.codewith.RabiaLinkApp.partners.domain.PartnerTransaction;
import com.codewith.RabiaLinkApp.partners.domain.PartnerTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerTransactionRepository extends JpaRepository<PartnerTransaction, Long> {
    
    List<PartnerTransaction> findByPartnerId(Long partnerId);
    
    List<PartnerTransaction> findByPartnerIdAndTransactionType(Long partnerId, PartnerTransactionType type);
    
    List<PartnerTransaction> findByInvoiceId(Long invoiceId);
    
    List<PartnerTransaction> findAll();
}
