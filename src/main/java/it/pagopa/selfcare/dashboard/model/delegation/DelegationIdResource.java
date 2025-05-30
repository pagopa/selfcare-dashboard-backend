package it.pagopa.selfcare.dashboard.model.delegation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DelegationIdResource {
    @ApiModelProperty(value = "${swagger.dashboard.delegation.model.id}")
    private String id;
}
