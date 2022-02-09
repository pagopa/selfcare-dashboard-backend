package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    private String name;
    private String surname;
    private String email;
    private String fiscalCode;
    private boolean certification;

}
