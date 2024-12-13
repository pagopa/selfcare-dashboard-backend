package it.pagopa.selfcare.dashboard.model;

import io.swagger.annotations.ApiModelProperty;

import lombok.Data;
@Data
public class SupportContactResource {
    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.supportContact.supportEmail}")
    private String supportEmail;
    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.supportContact.supportPhone}")
    private String supportPhone;
}
