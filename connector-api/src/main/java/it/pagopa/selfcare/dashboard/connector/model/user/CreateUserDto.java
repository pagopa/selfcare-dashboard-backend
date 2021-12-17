package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDto {

    private String name;
    private String surname;
    private String taxCode;
    private String productRole;
    private String email;
    private String partyRole;

}