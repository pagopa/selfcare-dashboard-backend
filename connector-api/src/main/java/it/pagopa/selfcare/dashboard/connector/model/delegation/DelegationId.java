package it.pagopa.selfcare.dashboard.connector.model.delegation;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class DelegationId {
    @NotNull
    private String id;

}
