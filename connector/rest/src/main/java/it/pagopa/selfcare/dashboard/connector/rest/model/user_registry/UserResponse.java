package it.pagopa.selfcare.dashboard.connector.rest.model.user_registry;

import it.pagopa.selfcare.dashboard.connector.model.user.Certification;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {

    private String id;
    private String externalId;
    private String name;
    private String surname;
    private Certification certification;
    private UserExtras extras;

}
