package it.pagopa.selfcare.dashboard.model.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WorkContactResource {
    @ApiModelProperty(value = "${swagger.dashboard.user.model.institutionalEmail}")
    private CertifiedFieldResource<String> email;
}
