package it.pagopa.selfcare.dashboard.connector.rest.model.onboarding;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OnboardingData {

    private String institutionId;
    private String description;
    private String digitalAddress;
    private RelationshipState state;
    private PartyRole role;
    private List<String> relationshipProducts;
    private String productRole;
    private List<String> institutionProducts;
    private List<String> attributes;

}
