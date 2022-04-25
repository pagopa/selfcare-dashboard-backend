package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

import java.util.Map;

@Data
public class MutableUserFieldsDto {

    private CertifiableFieldResource<String> name;
    private CertifiableFieldResource<String> familyName;
    private CertifiableFieldResource<String> email;
//TODO    private CertifiableFieldResource<LocalDate> birthDate;
    private Map<String, WorkContactResource> workContacts;

}
