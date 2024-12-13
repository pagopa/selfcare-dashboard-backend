package it.pagopa.selfcare.dashboard.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class InstitutionBaseResource {

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.id}")
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.name}")
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.model.userRole}")
    private String userRole;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.status}")
    private String status;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.parentDescription}")
    private String parentDescription;

}
