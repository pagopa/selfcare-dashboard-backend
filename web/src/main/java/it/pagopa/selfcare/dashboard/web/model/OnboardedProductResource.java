package it.pagopa.selfcare.dashboard.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.selfcare.dashboard.connector.model.institution.Billing;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import lombok.Data;

import java.util.List;

@Data
public class OnboardedProductResource {
    private String productId;
    private String userRole;

    @JsonProperty("productOnBoardingStatus")
    private RelationshipState status;

    private boolean authorized;
    private Billing billing;

    private List<String> userProductActions;
}
