package it.pagopa.selfcare.dashboard.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionType;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class InstitutionResource {

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.externalId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String externalId;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.originId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String originId;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.origin}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String origin;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.institutionType}", required = true)
    private InstitutionType institutionType;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.category}")
    private String category;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.fiscalCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String fiscalCode;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.mailAddress}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String mailAddress;

    @ApiModelProperty(value = "${swagger.dashboard.model.userRole}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String userRole;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.status}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String status;

}
