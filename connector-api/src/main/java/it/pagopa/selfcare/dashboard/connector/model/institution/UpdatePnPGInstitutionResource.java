package it.pagopa.selfcare.dashboard.connector.model.institution;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdatePnPGInstitutionResource {

    private String description;
    private String digitalAddress;

}
