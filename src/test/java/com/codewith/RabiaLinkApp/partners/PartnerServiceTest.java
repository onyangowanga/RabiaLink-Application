package com.codewith.RabiaLinkApp.partners;

import com.codewith.RabiaLinkApp.partners.domain.Partner;
import com.codewith.RabiaLinkApp.partners.domain.PartnerStatus;
import com.codewith.RabiaLinkApp.partners.domain.PartnerTransaction;
import com.codewith.RabiaLinkApp.partners.domain.PartnerTransactionType;
import com.codewith.RabiaLinkApp.partners.dto.PartnerResponse;
import com.codewith.RabiaLinkApp.partners.dto.PartnerTransactionResponse;
import com.codewith.RabiaLinkApp.partners.repository.PartnerRepository;
import com.codewith.RabiaLinkApp.partners.repository.PartnerTransactionRepository;
import com.codewith.RabiaLinkApp.partners.service.impl.PartnerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Partner Service Tests")
class PartnerServiceTest {

    @Mock
    private PartnerRepository partnerRepository;

    @Mock
    private PartnerTransactionRepository transactionRepository;

    @InjectMocks
    private PartnerServiceImpl partnerService;

    private Partner partner;
    private PartnerTransaction capitalTransaction;
    private PartnerTransaction profitTransaction;

    @BeforeEach
    void setUp() {
        // Setup test partner
        partner = new Partner();
        partner.setId(1L);
        partner.setPartnerCode("P-001");
        partner.setPartnerName("Test Partner");
        partner.setEmail("partner@test.com");
        partner.setPhone("1234567890");
        partner.setStatus(PartnerStatus.ACTIVE);
        partner.setTotalCapital(new BigDecimal("50000.00"));
        partner.setTotalProfitEarned(BigDecimal.ZERO);
        partner.setTotalDividendsPaid(BigDecimal.ZERO);
        partner.setProfitSharePercentage(new BigDecimal("20.00"));
        partner.setCreatedAt(LocalDateTime.now());

        // Setup capital transaction
        capitalTransaction = new PartnerTransaction();
        capitalTransaction.setId(1L);
        capitalTransaction.setPartner(partner);
        capitalTransaction.setTransactionType(PartnerTransactionType.CAPITAL_CONTRIBUTION);
        capitalTransaction.setAmount(new BigDecimal("50000.00"));
        capitalTransaction.setDescription("Initial capital contribution");
        capitalTransaction.setCreatedAt(LocalDateTime.now());

        // Setup profit transaction
        profitTransaction = new PartnerTransaction();
        profitTransaction.setId(2L);
        profitTransaction.setPartner(partner);
        profitTransaction.setTransactionType(PartnerTransactionType.PROFIT_ALLOCATION);
        profitTransaction.setAmount(new BigDecimal("5000.00"));
        profitTransaction.setDescription("Monthly profit allocation");
        profitTransaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should register partner successfully")
    void testRegisterPartner_Success() {
        // Given
        when(partnerRepository.save(any(Partner.class))).thenReturn(partner);

        // When
        PartnerResponse response = partnerService.registerPartner(partner);

        // Then
        assertNotNull(response);
        assertEquals("P-001", response.getPartnerCode());
        assertEquals("Test Partner", response.getPartnerName());
        assertEquals(PartnerStatus.ACTIVE.toString(), response.getStatus());
        verify(partnerRepository, times(1)).save(any(Partner.class));
    }

    @Test
    @DisplayName("Should retrieve partner by ID successfully")
    void testGetPartnerById_Success() {
        // Given
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));

        // When
        PartnerResponse response = partnerService.getPartnerById(1L);

        // Then
        assertNotNull(response);
        assertEquals(partner.getId(), response.getId());
        assertEquals("Test Partner", response.getPartnerName());
    }

    @Test
    @DisplayName("Should return null when partner not found")
    void testGetPartnerById_NotFound() {
        // Given
        when(partnerRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        PartnerResponse response = partnerService.getPartnerById(999L);

        // Then
        assertNull(response);
    }

    @Test
    @DisplayName("Should allocate profit to partner")
    void testAllocateProfit_Success() {
        // Given
        BigDecimal profitAmount = new BigDecimal("10000.00");
        BigDecimal partnerShare = profitAmount.multiply(partner.getProfitSharePercentage()).divide(new BigDecimal("100"));

        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));
        when(transactionRepository.save(any(PartnerTransaction.class))).thenReturn(profitTransaction);
        when(partnerRepository.save(any(Partner.class))).thenReturn(partner);

        // When
        PartnerTransactionResponse response = partnerService.allocateProfitToPartner(1L, profitAmount);

        // Then
        assertNotNull(response);
        assertEquals(PartnerTransactionType.PROFIT_ALLOCATION.toString(), response.getTransactionType());
        verify(transactionRepository, times(1)).save(any(PartnerTransaction.class));
        verify(partnerRepository, times(1)).save(any(Partner.class));
    }

    @Test
    @DisplayName("Should record capital contribution")
    void testRecordCapitalContribution_Success() {
        // Given
        BigDecimal capitalAmount = new BigDecimal("50000.00");

        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));
        when(transactionRepository.save(any(PartnerTransaction.class))).thenReturn(capitalTransaction);
        when(partnerRepository.save(any(Partner.class))).thenReturn(partner);

        // When
        PartnerTransactionResponse response = partnerService.recordCapitalContribution(1L, capitalAmount);

        // Then
        assertNotNull(response);
        assertEquals(PartnerTransactionType.CAPITAL_CONTRIBUTION.toString(), response.getTransactionType());
        assertEquals(capitalAmount, response.getAmount());
    }

    @Test
    @DisplayName("Should prevent profit allocation to inactive partner")
    void testAllocateProfit_InactivePartner_ShouldFail() {
        // Given
        partner.setStatus(PartnerStatus.INACTIVE);
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> partnerService.allocateProfitToPartner(1L, new BigDecimal("5000.00")),
            "Cannot allocate profit to inactive partner");
    }

    @Test
    @DisplayName("Should calculate correct profit share percentage")
    void testCalculateProfitShare_Correct() {
        // Given
        BigDecimal totalProfit = new BigDecimal("10000.00");
        BigDecimal expectedShare = totalProfit.multiply(new BigDecimal("20")).divide(new BigDecimal("100"));

        // When
        BigDecimal actualShare = partnerService.calculatePartnerShare(partner.getProfitSharePercentage(), totalProfit);

        // Then
        assertEquals(expectedShare, actualShare);
    }

    @Test
    @DisplayName("Should prevent capital contribution below minimum")
    void testRecordCapitalContribution_BelowMinimum_ShouldFail() {
        // Given
        BigDecimal lowCapital = new BigDecimal("1000.00"); // Below expected minimum
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));

        // When & Then
        assertThrows(RuntimeException.class,
            () -> partnerService.recordCapitalContribution(1L, lowCapital),
            "Capital contribution below minimum threshold");
    }

    @Test
    @DisplayName("Should prevent capital contribution exceeding maximum")
    void testRecordCapitalContribution_ExceedsMaximum_ShouldFail() {
        // Given
        BigDecimal highCapital = new BigDecimal("500000.00"); // Exceeds max
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));

        // When & Then
        assertThrows(RuntimeException.class,
            () -> partnerService.recordCapitalContribution(1L, highCapital),
            "Capital contribution exceeds maximum threshold");
    }

    @Test
    @DisplayName("Should retrieve all partner transactions")
    void testGetPartnerTransactions_Success() {
        // Given
        List<PartnerTransaction> transactions = new ArrayList<>();
        transactions.add(capitalTransaction);
        transactions.add(profitTransaction);

        when(transactionRepository.findByPartnerId(1L)).thenReturn(transactions);

        // When
        List<PartnerTransactionResponse> responses = partnerService.getPartnerTransactions(1L);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    @DisplayName("Should update partner status")
    void testUpdatePartnerStatus_Success() {
        // Given
        partner.setStatus(PartnerStatus.SUSPENDED);
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));
        when(partnerRepository.save(any(Partner.class))).thenReturn(partner);

        // When
        PartnerResponse response = partnerService.updatePartnerStatus(1L, PartnerStatus.SUSPENDED);

        // Then
        assertNotNull(response);
        assertEquals(PartnerStatus.SUSPENDED.toString(), response.getStatus());
    }

    @Test
    @DisplayName("Should calculate total capital correctly")
    void testCalculateTotalCapital_Correct() {
        // Given
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));

        // When
        BigDecimal totalCapital = partnerService.getTotalCapital(1L);

        // Then
        assertEquals(new BigDecimal("50000.00"), totalCapital);
    }
}
