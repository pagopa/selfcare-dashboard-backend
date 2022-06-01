package it.pagopa.selfcare.dashboard.connector.rest.model;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionType;
import lombok.Data;

@Data
public class InstitutionUpdate {

    private InstitutionType institutionType;
    private String description;
    private String digitalAddress;
    private String address;
    private String taxCode;

}
