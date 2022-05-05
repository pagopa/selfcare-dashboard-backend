package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

import java.util.Map;

@Data
public class MutableUserFieldsDto {

    private CertifiedField<String> name;
    private CertifiedField<String> familyName;
    private CertifiedField<String> email;
    //TODO    private CertifiedField<LocalDate> birthDate;
    private Map<String, WorkContactResource> workContacts;

}
