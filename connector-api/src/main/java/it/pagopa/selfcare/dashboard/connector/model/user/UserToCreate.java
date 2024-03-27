package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

import java.util.Set;

@Data
public class UserToCreate {
    private String name;
    private String surname;
    private String taxCode;
    private String email;
    private Set<String> productRoles;
}
