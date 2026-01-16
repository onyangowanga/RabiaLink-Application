package com.codewith.RabiaLinkApp.partners.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partners")
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String partnerName;

    @Column(nullable = false, unique = true)
    private String partnerCode;  // e.g., PART-001, PART-002

    // Partner's profit share percentage (0-100)
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal profitSharePercentage;

    // Capital contribution tracking
    @Column(precision = 19, scale = 2)
    private BigDecimal totalCapitalContributed;

    // Cumulative profit earned
    @Column(precision = 19, scale = 2)
    private BigDecimal totalProfitEarned;

    // Status
    @Enumerated(EnumType.STRING)
    private PartnerStatus status;  // ACTIVE, INACTIVE, SUSPENDED

    // Contact information
    private String contactEmail;
    private String contactPhone;
    private String address;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String createdBy;

    // Partner transactions for audit trail
    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartnerTransaction> transactions = new ArrayList<>();

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PartnerStatus.ACTIVE;
        }
        if (this.totalCapitalContributed == null) {
            this.totalCapitalContributed = BigDecimal.ZERO;
        }
        if (this.totalProfitEarned == null) {
            this.totalProfitEarned = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }

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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<PartnerTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<PartnerTransaction> transactions) {
        this.transactions = transactions;
    }
}
