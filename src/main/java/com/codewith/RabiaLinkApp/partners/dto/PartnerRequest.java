package com.codewith.RabiaLinkApp.partners.dto;

import com.codewith.RabiaLinkApp.partners.domain.PartnerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PartnerRequest {

    @NotBlank(message = "Partner name is required")
    private String partnerName;

    @NotBlank(message = "Partner code is required")
    private String partnerCode;

    // Profit share percentage is auto-calculated based on capital contributions
    // No need to provide it during registration

    private PartnerStatus status;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String createdBy;

    // Getters and setters
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
