package it.pagopa.selfcare.dashboard.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class InstitutionResource {

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.id}", required = true)
    @JsonProperty(required = true)
    private String id;
    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.name}", required = true)
    @JsonProperty(required = true)
    private String name;
    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.category}", required = true)
    @JsonProperty(required = true)
    private String category;
    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.IPACode}", required = true)
    @JsonProperty(required = true)
    private String IPACode;
    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.fiscalCode}", required = true)
    @JsonProperty(required = true)
    private String fiscalCode;
    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.mailAddress}", required = true)
    @JsonProperty(required = true)
    private String mailAddress;
    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.userRole}", required = true)
    @JsonProperty(required = true)
    private String userRole;
    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.status}", required = true)
    @JsonProperty(required = true)
    private String status;
}
