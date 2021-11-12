package it.pagopa.selfcare.dashboard.web;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DummyInstitutionInfo implements InstitutionInfo {
    private String institutionId;

    private String description;

    private String digitalAddress;

    private String status;

    private String role;

    private String platformRole;

    private List<String> activeProducts;

}