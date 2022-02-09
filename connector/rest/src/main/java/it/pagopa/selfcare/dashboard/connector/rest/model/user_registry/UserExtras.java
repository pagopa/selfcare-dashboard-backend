package it.pagopa.selfcare.dashboard.connector.rest.model.user_registry;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserExtras {
    private String email;
    private LocalDate birthDate;
}
