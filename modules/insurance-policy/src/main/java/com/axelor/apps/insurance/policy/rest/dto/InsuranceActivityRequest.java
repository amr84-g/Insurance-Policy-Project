/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.insurance.policy.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InsuranceActivityRequest {
  @JsonProperty("Activity")
  public ActivityDto activity;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ActivityDto {
    @JsonProperty("ActivityDate")
    public String activityDate;

    @JsonProperty("PolicyId")
    public String policyId;

    @JsonProperty("ActivityId")
    public String activityId;

    @JsonProperty("ActivityKey")
    public String activityKey;

    @JsonProperty("ActivityType")
    public String activityType;

    @JsonProperty("Branch")
    public String branch;

    @JsonProperty("CustomerId")
    public String customerId;

    @JsonProperty("IssueDate")
    public String issueDate;

    @JsonProperty("Effective")
    public String effectiveDate;

    @JsonProperty("EndDate")
    public String endDate;

    @JsonProperty("InsuranceType")
    public String insuranceType;

    @JsonProperty("MarketSource")
    public String marketSource;

    @JsonProperty("NetAmount")
    public BigDecimal netAmount;

    @JsonProperty("PolicyKey")
    public String policyKey;

    @JsonProperty("ProductOfInsurance")
    public String productOfInsurance;

    @JsonProperty("RefundFlag")
    public Boolean refundFlag;

    @JsonProperty("Sector")
    public String sector;

    @JsonProperty("TypeOfPolicy")
    public String typeOfPolicy;

    @JsonProperty("VendorId")
    public String vendorId;

    @JsonProperty("Zone")
    public String zone;

    @JsonProperty("Installments")
    public List<InstallmentDto> installments;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class InstallmentDto {
    @JsonProperty("CurrencyCode")
    public String currencyCode;

    @JsonProperty("CurrencyExchRate")
    public BigDecimal currencyExchangeRate;

    @JsonProperty("DueDate")
    public String dueDate;

    @JsonProperty("InstallmentId")
    public String installmentId;

    @JsonProperty("NetAmount")
    public BigDecimal netAmount;

    @JsonProperty("Brokers")
    public List<BrokerDto> brokers;

    @JsonProperty("Lines")
    public List<InstallmentLineDto> lines;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BrokerDto {
    @JsonProperty("BrokerCode")
    public String brokerCode;

    @JsonProperty("BrokerId")
    public String brokerId;

    @JsonProperty("BrokerShare")
    public BigDecimal brokerShare;

    @JsonProperty("CommissionAmount")
    public BigDecimal commissionAmount;

    @JsonProperty("CommissionKey")
    public String commissionKey;

    @JsonProperty("CommissionPercentage")
    public BigDecimal commissionPercentage;

    @JsonProperty("CommissionType")
    public String commissionType;

    @JsonProperty("GrossSection")
    public BigDecimal grossSection;

    @JsonProperty("PolicySectionId")
    public String policySectionId;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class InstallmentLineDto {
    @JsonProperty("Amount")
    public BigDecimal amount;

    @JsonProperty("AprId")
    public String aprId;

    @JsonProperty("AprKey")
    public String aprKey;

    @JsonProperty("Description")
    public String description;
  }
}
