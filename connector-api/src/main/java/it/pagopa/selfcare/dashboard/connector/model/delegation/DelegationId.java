package it.pagopa.selfcare.dashboard.connector.model.delegation;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DelegationId {
    @NotBlank
    private String id;

}
