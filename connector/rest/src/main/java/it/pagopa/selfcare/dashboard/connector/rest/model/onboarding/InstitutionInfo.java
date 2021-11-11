package it.pagopa.selfcare.dashboard.connector.rest.model.onboarding;

import lombok.Data;

import java.util.List;
/**
 * InstitutionInfo
 */
@Data
public class InstitutionInfo {

    private String institutionId;
    private String description;
    private String digitalAddress;
    private String status;
    private String role;
    private String platformRole;
    private List<String> attributes;

}
