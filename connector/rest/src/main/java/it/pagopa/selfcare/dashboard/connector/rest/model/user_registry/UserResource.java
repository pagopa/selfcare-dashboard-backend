package it.pagopa.selfcare.dashboard.connector.rest.model.user_registry;

import it.pagopa.selfcare.dashboard.connector.model.user.CertifiableFieldResource;
import it.pagopa.selfcare.dashboard.connector.model.user.WorkContactResource;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

@Data
@FieldNameConstants(asEnum = true)
public class UserResource {

    @NotNull
    @FieldNameConstants.Exclude
    private UUID id;
    private String fiscalCode;
    private CertifiableFieldResource<String> name;
    private CertifiableFieldResource<String> familyName;
    private CertifiableFieldResource<String> email;
    private Map<String, WorkContactResource> workContacts;

}