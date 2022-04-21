package it.pagopa.selfcare.dashboard.connector.rest.model.user_registry;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserExtras {
    private String email;
    private LocalDate birthDate;
}
