package it.pagopa.selfcare.dashboard.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class InstitutionResource {

    @ApiModelProperty("${swagger.dashboard.institutions.model.id}")
    private String id;
    @ApiModelProperty("${swagger.dashboard.institutions.model.name}")
    private String name;
    @ApiModelProperty("${swagger.dashboard.institutions.model.type}")
    private String type;
    @ApiModelProperty("${swagger.dashboard.institutions.model.IPACode}")
    private String IPACode;
    @ApiModelProperty("${swagger.dashboard.institutions.model.fiscalCode}")
    private String fiscalCode;
    @ApiModelProperty("${swagger.dashboard.institutions.model.mailAddress}")
    private String mailAddress;
}
