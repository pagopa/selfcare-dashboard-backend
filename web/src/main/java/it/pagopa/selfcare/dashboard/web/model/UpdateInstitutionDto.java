package it.pagopa.selfcare.dashboard.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UpdateInstitutionDto {

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.description}")
    private String description;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.digitalAddress}")
    private String digitalAddress;


}
