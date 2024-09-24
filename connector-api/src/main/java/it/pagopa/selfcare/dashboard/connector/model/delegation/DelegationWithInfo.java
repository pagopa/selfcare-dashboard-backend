package it.pagopa.selfcare.dashboard.connector.model.delegation;

import it.pagopa.selfcare.onboarding.common.InstitutionType;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class DelegationWithInfo {
    private String id;
    private String institutionId;
    private String institutionName;
    private String brokerId;
    private String brokerName;
    private String brokerTaxCode;
    private String brokerType;
    private String productId;
    private String institutionRootName;
    private DelegationType type;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String institutionType;
    private String status;
    private String taxCode;
}
