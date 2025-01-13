package it.pagopa.selfcare.dashboard.web.model.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class GetUserResource {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.id}")
    private UUID id;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}")
    private CertifiedFieldResource<String> name;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}")
    private CertifiedFieldResource<String> familyName;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.institutionalEmail}")
    private CertifiedFieldResource<String> email;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.fiscalCode}")
    private String fiscalCode;

}
