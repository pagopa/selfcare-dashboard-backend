package it.pagopa.selfcare.dashboard.web;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DummyInstitutionInfo implements InstitutionInfo {
    private String institutionId;

    private String description;

    private String digitalAddress;

    private String status;

    private String role;

    private String platformRole;
}