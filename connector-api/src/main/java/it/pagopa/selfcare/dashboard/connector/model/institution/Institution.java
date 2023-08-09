package it.pagopa.selfcare.dashboard.connector.model.institution;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(of = "id")
public class Institution {

    private String id;
    private String externalId;
    private String originId;
    private String description;
    private String digitalAddress;
    private String address;
    private String zipCode;
    private String taxCode;
    private String origin;
    private InstitutionType institutionType;
    private List<Attribute> attributes;
    private List<GeographicTaxonomy> geographicTaxonomies;
    private String category;
    private Billing billing;
    private PaymentServiceProvider paymentServiceProvider;
    private DataProtectionOfficer dataProtectionOfficer;
    private SupportContact supportContact;
    private List<OnboardedProduct> onboarding;

}
