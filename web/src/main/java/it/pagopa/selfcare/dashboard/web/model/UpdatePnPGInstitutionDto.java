package it.pagopa.selfcare.dashboard.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UpdatePnPGInstitutionDto {

    @ApiModelProperty(value = "${swagger.dashboard.pnPGInstitutions.model.description}")
    private String description;

    @ApiModelProperty(value = "${swagger.dashboard.pnPGInstitutions.model.digitalAddress}")
    private String digitalAddress;


}
