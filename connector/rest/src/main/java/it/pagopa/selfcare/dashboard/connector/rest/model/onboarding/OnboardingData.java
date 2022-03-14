package it.pagopa.selfcare.dashboard.connector.rest.model.onboarding;

import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.user.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OnboardingData {

    private String institutionId;
    private String description;
    private String taxCode;
    private String digitalAddress;
    private RelationshipState state;
    private PartyRole role;
    private ProductInfo productInfo;
    private List<Attribute> attributes;

}
