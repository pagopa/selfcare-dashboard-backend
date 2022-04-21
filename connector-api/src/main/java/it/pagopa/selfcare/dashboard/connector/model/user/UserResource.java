package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.Map;
import java.util.UUID;

@Data
@FieldNameConstants(asEnum = true)
public class UserResource {

    @FieldNameConstants.Exclude
    private UUID id;
    private String fiscalCode;
    private CertifiableFieldResource<String> name;
    private CertifiableFieldResource<String> familyName;
    private CertifiableFieldResource<String> email;
    private Map<String, WorkContactResource> workContacts;


}