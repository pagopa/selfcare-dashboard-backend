package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

import java.util.Set;

@Data
public class CreateUserDto {

    private String name;
    private String surname;
    private String taxCode;
    private String email;
    private Set<Role> roles;

    @Data
    public static class Role {
        private String productRole;
        private String partyRole;
    }
}