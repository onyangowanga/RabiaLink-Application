package com.codewith.RabiaLinkApp.partners.repository;

import com.codewith.RabiaLinkApp.partners.domain.Partner;
import com.codewith.RabiaLinkApp.partners.domain.PartnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {
    
    Optional<Partner> findByPartnerCode(String partnerCode);
    
    Optional<Partner> findByPartnerName(String partnerName);
    
    List<Partner> findByStatus(PartnerStatus status);
    
    List<Partner> findAll();
}
