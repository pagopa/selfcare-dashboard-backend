package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

@Data
public class User {

    private String id;
    private String name;
    private String surname;
    private String email;
    private String fiscalCode;
    private boolean certification;

}
