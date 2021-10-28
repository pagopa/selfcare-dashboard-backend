package it.pagopa.selfcare.dashboard.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrganizationResource {

    @ApiModelProperty("${swagger.dashboard.model.id}")
    private String id;
    @ApiModelProperty("${swagger.dashboard.model.organizationName}")
    private String organizationName;
    @ApiModelProperty("${swagger.dashboard.model.organizationType}")
    private String organizationType;
    @ApiModelProperty("${swagger.dashboard.model.IPACode}")
    private String IPACode;
    @ApiModelProperty("${swagger.dashboard.model.fiscalCode}")
    private String fiscalCode;
    @ApiModelProperty("${swagger.dashboard.model.mailAddress}")
    private String mailAddress;
    @ApiModelProperty("${swagger.dashboard.model.logo}")
    private String logo;
}
