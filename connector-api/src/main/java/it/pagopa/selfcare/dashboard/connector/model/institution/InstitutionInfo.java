package it.pagopa.selfcare.dashboard.connector.model.institution;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstitutionInfo {

    private String institutionId;
    private String description;
    private String taxCode;
    private String digitalAddress;
    private String status;
    private String category;

}
