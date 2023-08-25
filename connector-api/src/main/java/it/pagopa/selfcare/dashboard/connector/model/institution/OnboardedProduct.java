package it.pagopa.selfcare.dashboard.connector.model.institution;

import lombok.Data;

@Data
public class OnboardedProduct {
    private String productId;
    private String userRole;
    private RelationshipState status;
    private boolean authorized;
    private Billing billing;
}
