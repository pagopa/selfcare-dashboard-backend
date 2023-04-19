package it.pagopa.selfcare.dashboard.connector.rest.model.onboarding;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.*;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.PnPGProductInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.ProductInfo;
import lombok.Data;

import java.util.List;

@Data
public class OnboardingPnPGData {

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
    private PnPGProductInfo productInfo;
    private InstitutionType institutionType;
    private String pricingPlan;
    private Billing billing;
    private String origin;
    private List<Attribute> attributes;
    private List<GeographicTaxonomy> geographicTaxonomies;

}
