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
package com.axelor.apps.insurance.policy.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.insurance.policy.db.ActivityTable;
import com.axelor.apps.insurance.policy.db.InstallmentLine;
import com.axelor.apps.insurance.policy.db.InstallmentTable;
import com.axelor.apps.insurance.policy.db.PolicyTable;
import com.axelor.apps.insurance.policy.db.SectionBrokerTable;
import com.axelor.apps.insurance.policy.db.SectionTable;
import com.axelor.apps.insurance.policy.db.repo.ActivityTableRepository;
import com.axelor.apps.insurance.policy.db.repo.InstallmentLineRepository;
import com.axelor.apps.insurance.policy.db.repo.InstallmentTableRepository;
import com.axelor.apps.insurance.policy.db.repo.PolicyTableRepository;
import com.axelor.apps.insurance.policy.db.repo.SectionBrokerTableRepository;
import com.axelor.apps.insurance.policy.db.repo.SectionTableRepository;
import com.axelor.apps.insurance.policy.rest.dto.InsuranceActivityRequest;
import com.axelor.apps.insurance.policy.rest.dto.InsuranceActivityRequest.ActivityDto;
import com.axelor.apps.insurance.policy.rest.dto.InsuranceActivityRequest.BrokerDto;
import com.axelor.apps.insurance.policy.rest.dto.InsuranceActivityRequest.InstallmentDto;
import com.axelor.apps.insurance.policy.rest.dto.InsuranceActivityRequest.InstallmentLineDto;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class InsuranceIntegrationService {
  private final PolicyTableRepository policyRepository;
  private final ActivityTableRepository activityRepository;
  private final SectionTableRepository sectionRepository;
  private final InstallmentTableRepository installmentRepository;
  private final SectionBrokerTableRepository sectionBrokerRepository;
  private final InstallmentLineRepository installmentLineRepository;
  private final PartnerRepository partnerRepository;

  @Inject
  public InsuranceIntegrationService(
      PolicyTableRepository policyRepository,
      ActivityTableRepository activityRepository,
      SectionTableRepository sectionRepository,
      InstallmentTableRepository installmentRepository,
      SectionBrokerTableRepository sectionBrokerRepository,
      InstallmentLineRepository installmentLineRepository,
      PartnerRepository partnerRepository) {
    this.policyRepository = policyRepository;
    this.activityRepository = activityRepository;
    this.sectionRepository = sectionRepository;
    this.installmentRepository = installmentRepository;
    this.sectionBrokerRepository = sectionBrokerRepository;
    this.installmentLineRepository = installmentLineRepository;
    this.partnerRepository = partnerRepository;
  }

  @Transactional(rollbackOn = Exception.class)
  public ActivityTable receiveActivity(InsuranceActivityRequest request) {
    if (request == null || request.activity == null) {
      throw new IllegalArgumentException("The Activity object is required.");
    }

    ActivityDto incoming = request.activity;
    requireText(incoming.policyId, "Activity.PolicyId");
    requireText(incoming.activityId, "Activity.ActivityId");

    PolicyTable policy = findOrCreatePolicy(incoming);
    policyRepository.save(policy);

    ActivityTable activity = createActivity(incoming, policy);
    activityRepository.save(activity);

    if (incoming.installments != null) {
      for (InstallmentDto incomingInstallment : incoming.installments) {
        if (incomingInstallment == null) {
          throw new IllegalArgumentException("Activity.Installments cannot contain null entries.");
        }
        createInstallmentSection(incomingInstallment, activity);
      }
    }

    return activity;
  }

  private PolicyTable findOrCreatePolicy(ActivityDto incoming) {
    PolicyTable policy =
        policyRepository
            .all()
            .filter("self.policyId = :policyId")
            .bind("policyId", incoming.policyId)
            .fetchOne();
    if (policy == null) {
      policy = new PolicyTable();
      policy.setPolicyId(incoming.policyId);
    }

    if (incoming.customerId != null) {
      policy.setCustomerId(incoming.customerId);
    }
    if (hasText(incoming.customerId)) {
      policy.setCustomer(partnerRepository.findByPartnerSeq(incoming.customerId.trim()));
    } else if (incoming.customerId != null) {
      policy.setCustomer(null);
    }
    if (incoming.typeOfPolicy != null) {
      policy.setTypeOfPolicy(selectionValue(incoming.typeOfPolicy));
    }
    if (incoming.insuranceType != null) {
      policy.setInsuranceType(selectionValue(incoming.insuranceType));
    }
    setDateIfPresent(incoming.issueDate, "Activity.IssueDate", policy::setIssueDate);
    setDateIfPresent(incoming.effectiveDate, "Activity.Effective", policy::setEffectiveDate);
    setDateIfPresent(incoming.endDate, "Activity.EndDate", policy::setEndDate);
    if (incoming.policyKey != null) {
      policy.setPolicyKey(incoming.policyKey);
    }
    if (incoming.zone != null) {
      policy.setZone(incoming.zone);
    }
    if (incoming.branch != null) {
      policy.setBranch(incoming.branch);
    }
    if (incoming.sector != null) {
      policy.setSector(incoming.sector);
    }
    if (incoming.productOfInsurance != null) {
      policy.setProductOfInsurance(incoming.productOfInsurance);
    }
    if (incoming.marketSource != null) {
      policy.setMarketSource(incoming.marketSource);
    }
    return policy;
  }

  private ActivityTable createActivity(ActivityDto incoming, PolicyTable policy) {
    ActivityTable activity = new ActivityTable();
    activity.setActivityId(incoming.activityId);
    activity.setPolicyId(incoming.policyId);
    activity.setPolicy(policy);
    activity.setActivityType(selectionValue(incoming.activityType));
    activity.setActivityDate(parseDate(incoming.activityDate, "Activity.ActivityDate"));
    activity.setIssueDate(parseDate(incoming.issueDate, "Activity.IssueDate"));
    activity.setEffectiveDate(parseDate(incoming.effectiveDate, "Activity.Effective"));
    activity.setEndDate(parseDate(incoming.endDate, "Activity.EndDate"));
    activity.setActivityKey(incoming.activityKey);
    activity.setRefundFlag(incoming.refundFlag);
    activity.setVendorId(incoming.vendorId);
    activity.setNetAmount(incoming.netAmount);
    return activity;
  }

  private void createInstallmentSection(InstallmentDto incoming, ActivityTable activity) {
    String sectionId = findSectionId(incoming);
    SectionTable section = new SectionTable();
    section.setSectionId(sectionId);
    section.setActivity(activity);
    section.setActivityId(activity.getActivityId());
    section.setCurrencyCode(incoming.currencyCode);
    section.setAmount(incoming.netAmount);
    section.setDueRate(incoming.currencyExchangeRate);
    section.setDueDate(parseDate(incoming.dueDate, "Installment.DueDate"));
    sectionRepository.save(section);

    InstallmentTable installment = new InstallmentTable();
    installment.setInstallmentId(incoming.installmentId);
    installment.setSection(section);
    installment.setSectionId(sectionId);
    installment.setInstallmentAmount(incoming.netAmount);
    installment.setDueDate(parseDate(incoming.dueDate, "Installment.DueDate"));
    installmentRepository.save(installment);

    createBrokers(incoming.brokers, section);
    createLines(incoming.lines, installment);
  }

  private void createBrokers(List<BrokerDto> brokers, SectionTable section) {
    if (brokers == null) {
      return;
    }
    for (BrokerDto incoming : brokers) {
      if (incoming == null) {
        throw new IllegalArgumentException("Installment.Brokers cannot contain null entries.");
      }
      SectionBrokerTable broker = new SectionBrokerTable();
      broker.setSection(section);
      broker.setSectionId(section.getSectionId());
      broker.setBrokerId(incoming.brokerId);
      broker.setBrokerCode(incoming.brokerCode);
      broker.setCommissionKey(incoming.commissionKey);
      broker.setBrokerShare(incoming.brokerShare);
      broker.setCommissionAmount(incoming.commissionAmount);
      broker.setCommissionPercentage(incoming.commissionPercentage);
      broker.setCommissionType(selectionValue(incoming.commissionType));
      broker.setGrossSection(incoming.grossSection);
      broker.setPolicySectionId(incoming.policySectionId);
      if (hasText(incoming.brokerId)) {
        Partner partner = partnerRepository.findByPartnerSeq(incoming.brokerId.trim());
        if (partner != null) {
          broker.setBroker(partner);
        }
      }
      sectionBrokerRepository.save(broker);
    }
  }

  private void createLines(List<InstallmentLineDto> lines, InstallmentTable installment) {
    if (lines == null) {
      return;
    }
    for (InstallmentLineDto incoming : lines) {
      if (incoming == null) {
        throw new IllegalArgumentException("Installment.Lines cannot contain null entries.");
      }
      InstallmentLine line = new InstallmentLine();
      line.setInstallment(installment);
      line.setAmount(incoming.amount);
      line.setAprId(incoming.aprId);
      line.setAprKey(incoming.aprKey);
      line.setDescription(incoming.description);
      installmentLineRepository.save(line);
    }
  }

  private String findSectionId(InstallmentDto installment) {
    if (installment.brokers != null) {
      for (BrokerDto broker : installment.brokers) {
        if (broker != null && hasText(broker.policySectionId)) {
          return broker.policySectionId;
        }
      }
    }
    return hasText(installment.installmentId) ? installment.installmentId : null;
  }

  private LocalDate parseDate(String value, String fieldName) {
    if (!hasText(value)) {
      return null;
    }
    try {
      return LocalDate.parse(value.trim());
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException(
          fieldName + " must be an ISO-8601 date (yyyy-MM-dd).", exception);
    }
  }

  private String selectionValue(String value) {
    if (!hasText(value)) {
      return value;
    }
    return value
        .trim()
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("(^-|-$)", "");
  }

  private void setDateIfPresent(
      String value, String fieldName, java.util.function.Consumer<LocalDate> setter) {
    if (hasText(value)) {
      setter.accept(parseDate(value, fieldName));
    }
  }

  private void requireText(String value, String fieldName) {
    if (!hasText(value)) {
      throw new IllegalArgumentException(fieldName + " is required.");
    }
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
