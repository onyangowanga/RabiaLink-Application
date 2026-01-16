package com.codewith.RabiaLinkApp.partners.service;

import java.math.BigDecimal;

public class PartnerReportResponse {

    private Long partnerId;
    private String partnerName;
    private String partnerCode;
    private BigDecimal profitSharePercentage;
    private BigDecimal totalCapitalContributed;
    private BigDecimal totalProfitEarned;
    private BigDecimal currentCapitalBalance;
    private BigDecimal netPosition;  // Capital + Profit
    private Integer totalTransactions;

    // Getters and setters
    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
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

    public BigDecimal getCurrentCapitalBalance() {
        return currentCapitalBalance;
    }

    public void setCurrentCapitalBalance(BigDecimal currentCapitalBalance) {
        this.currentCapitalBalance = currentCapitalBalance;
    }

    public BigDecimal getNetPosition() {
        return netPosition;
    }

    public void setNetPosition(BigDecimal netPosition) {
        this.netPosition = netPosition;
    }

    public Integer getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Integer totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
}
