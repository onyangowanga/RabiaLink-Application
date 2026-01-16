package com.codewith.RabiaLinkApp.partners.service.impl;

import com.codewith.RabiaLinkApp.common.exception.ResourceNotFoundException;
import com.codewith.RabiaLinkApp.partners.domain.Partner;
import com.codewith.RabiaLinkApp.partners.domain.PartnerStatus;
import com.codewith.RabiaLinkApp.partners.domain.PartnerTransaction;
import com.codewith.RabiaLinkApp.partners.domain.PartnerTransactionType;
import com.codewith.RabiaLinkApp.partners.dto.PartnerRequest;
import com.codewith.RabiaLinkApp.partners.dto.PartnerResponse;
import com.codewith.RabiaLinkApp.partners.dto.PartnerTransactionRequest;
import com.codewith.RabiaLinkApp.partners.dto.PartnerTransactionResponse;
import com.codewith.RabiaLinkApp.partners.repository.PartnerRepository;
import com.codewith.RabiaLinkApp.partners.repository.PartnerTransactionRepository;
import com.codewith.RabiaLinkApp.partners.service.PartnerReportResponse;
import com.codewith.RabiaLinkApp.partners.service.PartnerService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final PartnerTransactionRepository transactionRepository;

    public PartnerServiceImpl(
            PartnerRepository partnerRepository,
            PartnerTransactionRepository transactionRepository
    ) {
        this.partnerRepository = partnerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public PartnerResponse registerPartner(PartnerRequest request) {
        // Validate partner code uniqueness
        if (partnerRepository.findByPartnerCode(request.getPartnerCode()).isPresent()) {
            throw new IllegalArgumentException("Partner code already exists: " + request.getPartnerCode());
        }

        // Validate partner name uniqueness
        if (partnerRepository.findByPartnerName(request.getPartnerName()).isPresent()) {
            throw new IllegalArgumentException("Partner name already exists: " + request.getPartnerName());
        }

        Partner partner = new Partner();
        partner.setPartnerName(request.getPartnerName());
        partner.setPartnerCode(request.getPartnerCode());
        // Profit share percentage will be calculated based on capital contributions
        partner.setProfitSharePercentage(BigDecimal.ZERO);  // Initially 0% until capital is contributed
        partner.setStatus(request.getStatus() != null ? request.getStatus() : PartnerStatus.ACTIVE);
        partner.setContactEmail(request.getContactEmail());
        partner.setContactPhone(request.getContactPhone());
        partner.setAddress(request.getAddress());
        partner.setCreatedBy(request.getCreatedBy());

        Partner savedPartner = partnerRepository.save(partner);
        return mapToResponse(savedPartner);
    }

    @Override
    public PartnerResponse getPartnerById(Long partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + partnerId));
        return mapToResponse(partner);
    }

    @Override
    public List<PartnerResponse> getAllPartners() {
        return partnerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PartnerTransactionResponse recordCapitalContribution(PartnerTransactionRequest request) {
        Partner partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + request.getPartnerId()));

        // Create transaction
        PartnerTransaction transaction = new PartnerTransaction();
        transaction.setPartner(partner);
        transaction.setTransactionType(PartnerTransactionType.CAPITAL_CONTRIBUTION);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now());
        transaction.setCreatedBy(request.getCreatedBy());

        PartnerTransaction savedTransaction = transactionRepository.save(transaction);

        // Update partner's total capital
        partner.setTotalCapitalContributed(
            partner.getTotalCapitalContributed().add(request.getAmount())
        );
        partnerRepository.save(partner);

        // Recalculate all partners' profit share percentages based on capital contributions
        recalculateAllPartnersProfitShares();

        return mapTransactionToResponse(savedTransaction);
    }

    /**
     * Recalculate profit share percentages for all partners based on their capital contributions
     * Profit Share % = (Partner's Capital / Total Capital) × 100
     */
    private void recalculateAllPartnersProfitShares() {
        List<Partner> allPartners = partnerRepository.findAll();

        // Calculate total capital contributed by all partners
        BigDecimal totalCapital = allPartners.stream()
                .map(Partner::getTotalCapitalContributed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // If no capital contributed, all get 0% share
        if (totalCapital.compareTo(BigDecimal.ZERO) == 0) {
            for (Partner partner : allPartners) {
                partner.setProfitSharePercentage(BigDecimal.ZERO);
            }
        } else {
            // Calculate each partner's share
            for (Partner partner : allPartners) {
                BigDecimal sharePercentage = partner.getTotalCapitalContributed()
                        .divide(totalCapital, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100))
                        .setScale(2, RoundingMode.HALF_UP);
                partner.setProfitSharePercentage(sharePercentage);
            }
        }

        partnerRepository.saveAll(allPartners);
    }

    @Override
    public void allocateProfitToPartners(Long invoiceId, BigDecimal supplierCost) {
        // Get all active partners
        List<Partner> activePartners = partnerRepository.findByStatus(PartnerStatus.ACTIVE);

        if (activePartners.isEmpty()) {
            return;  // No partners to allocate profit to
        }

        // Calculate gross profit (Invoice Amount - Supplier Cost)
        // For this implementation, we assume the invoiceId links to total invoice amount
        // In a real scenario, fetch the invoice and get its total amount
        
        // For now, we'll use a simplified approach where profit is passed explicitly
        // The actual profit calculation would be: totalInvoiceAmount - supplierCost

        for (Partner partner : activePartners) {
            // Calculate partner's share of profit
            // Profit Share = (Gross Profit) * (Partner Share % / 100)
            BigDecimal profitShare = supplierCost
                    .multiply(partner.getProfitSharePercentage())
                    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);

            // Record profit allocation transaction
            PartnerTransaction transaction = new PartnerTransaction();
            transaction.setPartner(partner);
            transaction.setTransactionType(PartnerTransactionType.PROFIT_ALLOCATION);
            transaction.setAmount(profitShare);
            transaction.setInvoiceId(invoiceId);
            transaction.setDescription("Profit allocation from invoice " + invoiceId);
            transaction.setTransactionDate(LocalDateTime.now());

            transactionRepository.save(transaction);

            // Update partner's total profit earned
            partner.setTotalProfitEarned(
                partner.getTotalProfitEarned().add(profitShare)
            );
            partnerRepository.save(partner);
        }
    }

    @Override
    public List<PartnerTransactionResponse> getPartnerTransactions(Long partnerId) {
        return transactionRepository.findByPartnerId(partnerId).stream()
                .map(this::mapTransactionToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getPartnerCapitalBalance(Long partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + partnerId));
        return partner.getTotalCapitalContributed();
    }

    @Override
    public BigDecimal getPartnerTotalProfit(Long partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + partnerId));
        return partner.getTotalProfitEarned();
    }

    @Override
    public PartnerReportResponse getPartnerReport(Long partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + partnerId));

        PartnerReportResponse report = new PartnerReportResponse();
        report.setPartnerId(partner.getId());
        report.setPartnerName(partner.getPartnerName());
        report.setPartnerCode(partner.getPartnerCode());
        report.setProfitSharePercentage(partner.getProfitSharePercentage());
        report.setTotalCapitalContributed(partner.getTotalCapitalContributed());
        report.setTotalProfitEarned(partner.getTotalProfitEarned());
        report.setCurrentCapitalBalance(partner.getTotalCapitalContributed());
        
        // Net position = Total Capital + Total Profit Earned
        BigDecimal netPosition = partner.getTotalCapitalContributed()
                .add(partner.getTotalProfitEarned());
        report.setNetPosition(netPosition);
        
        report.setTotalTransactions(partner.getTransactions().size());

        return report;
    }

    /**
     * Map Partner entity to PartnerResponse DTO
     */
    private PartnerResponse mapToResponse(Partner partner) {
        PartnerResponse response = new PartnerResponse();
        response.setId(partner.getId());
        response.setPartnerName(partner.getPartnerName());
        response.setPartnerCode(partner.getPartnerCode());
        response.setProfitSharePercentage(partner.getProfitSharePercentage());
        response.setTotalCapitalContributed(partner.getTotalCapitalContributed());
        response.setTotalProfitEarned(partner.getTotalProfitEarned());
        response.setStatus(partner.getStatus());
        response.setContactEmail(partner.getContactEmail());
        response.setContactPhone(partner.getContactPhone());
        response.setAddress(partner.getAddress());
        response.setCreatedAt(partner.getCreatedAt());
        response.setModifiedAt(partner.getModifiedAt());
        return response;
    }

    /**
     * Map PartnerTransaction entity to PartnerTransactionResponse DTO
     */
    private PartnerTransactionResponse mapTransactionToResponse(PartnerTransaction transaction) {
        PartnerTransactionResponse response = new PartnerTransactionResponse();
        response.setId(transaction.getId());
        response.setPartnerId(transaction.getPartner().getId());
        response.setPartnerCode(transaction.getPartner().getPartnerCode());
        response.setTransactionType(transaction.getTransactionType());
        response.setAmount(transaction.getAmount());
        response.setInvoiceId(transaction.getInvoiceId());
        response.setSupplierId(transaction.getSupplierId());
        response.setDescription(transaction.getDescription());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }
}
