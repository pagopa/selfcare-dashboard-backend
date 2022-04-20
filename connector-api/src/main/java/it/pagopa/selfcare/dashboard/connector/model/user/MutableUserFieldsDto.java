package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class MutableUserFieldsDto {

    private CertifiableFieldResource<String> name;
    private CertifiableFieldResource<String> familyName;
    private CertifiableFieldResource<String> email;
    private CertifiableFieldResource<LocalDate> birthDate;
    private Map<String, WorkContactResource> workContacts;

}
