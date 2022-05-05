package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.Map;

@Data
@FieldNameConstants(asEnum = true)
public class UserResource {

    @FieldNameConstants.Exclude
    private String id;
    private String fiscalCode;
    private CertifiedField<String> name;
    private CertifiedField<String> familyName;
    private CertifiedField<String> email;
    private Map<String, WorkContactResource> workContacts;


}