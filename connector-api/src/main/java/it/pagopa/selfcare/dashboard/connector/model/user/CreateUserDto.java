package it.pagopa.selfcare.dashboard.connector.model.user;

import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import lombok.Data;

import java.util.Set;

@Data
public class CreateUserDto {

    private String name;
    private String surname;
    private String taxCode;
    private String email;
    private Set<Role> roles;
    private SaveUserDto user;

    @Data
    public static class Role {
        private String productRole;
        private PartyRole partyRole;
    }
}