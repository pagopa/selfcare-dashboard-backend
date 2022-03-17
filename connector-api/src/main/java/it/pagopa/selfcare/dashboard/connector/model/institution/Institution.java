package it.pagopa.selfcare.dashboard.connector.model.institution;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class Institution {

    private String id;
    private String institutionId;
    private String description;
    private String digitalAddress;
    private String address;
    private String zipCode;
    private String taxCode;

}
