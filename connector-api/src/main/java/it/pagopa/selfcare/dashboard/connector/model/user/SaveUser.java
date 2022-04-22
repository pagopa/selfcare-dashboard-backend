package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

import java.util.Map;

@Data
public class SaveUser {
    private String name;
    private String familyName;
    private String email;
    private String fiscalCode;
    private Map<String, WorkContact> workContacts;
}
