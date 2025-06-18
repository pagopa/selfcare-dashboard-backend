package it.pagopa.selfcare.dashboard.model.institution;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OnboardedProduct {
    private String productId;
    private String userRole;
    private RelationshipState status;
    private boolean authorized;
    private Billing billing;
    private List<String> userProductActions;
    private Boolean isAggregator;
    private String origin;
    private String originId;
    private String institutionType;
}
