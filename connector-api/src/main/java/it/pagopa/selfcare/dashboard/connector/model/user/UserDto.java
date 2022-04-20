package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class UserDto {
    private String name;
    private String familyName;
    private String email;
    private LocalDate birthDate;
    private Map<String, WorkContact> workContacts;
}
