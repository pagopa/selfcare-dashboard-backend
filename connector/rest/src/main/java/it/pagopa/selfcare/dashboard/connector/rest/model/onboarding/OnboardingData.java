package it.pagopa.selfcare.dashboard.connector.rest.model.onboarding;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.Attribute;
import it.pagopa.selfcare.dashboard.connector.model.institution.Billing;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionType;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.ProductInfo;
import lombok.Data;

import java.util.List;

@Data
public class OnboardingData {

    private String id;
    private String externalId;
    private String originId;
    private String description;
    private String taxCode;
    private String digitalAddress;
    private String address;
    private String zipCode;
    private RelationshipState state;
    private PartyRole role;
    private ProductInfo productInfo;
    private InstitutionType institutionType;
    private String pricingPlan;
    private Billing billing;
    private String origin;
    private List<Attribute> attributes;

}
