package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

import java.time.LocalDate;

@Data
public class User {

    private String id;
    private CertifiableField<String> name;
    private CertifiableField<String> familyName;
    private CertifiableField<String> email;
    private CertifiableField<LocalDate> birthDate;
    private String fiscalCode;
    private CertifiableField<String> workContact;

}
