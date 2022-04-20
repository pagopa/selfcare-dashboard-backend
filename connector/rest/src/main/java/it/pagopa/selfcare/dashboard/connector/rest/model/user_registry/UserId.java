package it.pagopa.selfcare.dashboard.connector.rest.model.user_registry;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class UserId {

    @NotNull
    private UUID id;

}
