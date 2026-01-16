package com.codewith.RabiaLinkApp.partners.dto;

import com.codewith.RabiaLinkApp.partners.domain.PartnerStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PartnerResponse {

    private Long id;
    private String partnerName;
    private String partnerCode;
    private BigDecimal profitSharePercentage;
    private BigDecimal totalCapitalContributed;
    private BigDecimal totalProfitEarned;
    private PartnerStatus status;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    public BigDecimal getProfitSharePercentage() {
        return profitSharePercentage;
    }

    public void setProfitSharePercentage(BigDecimal profitSharePercentage) {
        this.profitSharePercentage = profitSharePercentage;
    }

    public BigDecimal getTotalCapitalContributed() {
        return totalCapitalContributed;
    }

    public void setTotalCapitalContributed(BigDecimal totalCapitalContributed) {
        this.totalCapitalContributed = totalCapitalContributed;
    }

    public BigDecimal getTotalProfitEarned() {
        return totalProfitEarned;
    }

    public void setTotalProfitEarned(BigDecimal totalProfitEarned) {
        this.totalProfitEarned = totalProfitEarned;
    }

    public PartnerStatus getStatus() {
        return status;
    }

    public void setStatus(PartnerStatus status) {
        this.status = status;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
