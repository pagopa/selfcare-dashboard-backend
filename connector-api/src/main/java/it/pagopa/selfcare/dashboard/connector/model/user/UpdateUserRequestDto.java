package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

@Data
public class UpdateUserRequestDto {
    private String name;
    private String surname;
    private String email;
    private String mobilePhone;
}
