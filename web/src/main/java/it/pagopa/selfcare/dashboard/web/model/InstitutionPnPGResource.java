package it.pagopa.selfcare.dashboard.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class InstitutionPnPGResource {

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
//    @NotBlank
    private String originId;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.origin}", required = true)
    @JsonProperty(required = true)
//    @NotBlank
    private String origin;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.institutionType}")
    private String institutionType;

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
//    @NotBlank
    private String mailAddress;

    @ApiModelProperty(value = "${swagger.dashboard.model.userRole}", required = true)
    @JsonProperty(required = true)
//    @NotBlank
    private String userRole;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.status}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String status;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.address}", required = true)
    @JsonProperty(required = true)
//    @NotBlank
    private String address;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.zipCode}", required = true)
    @JsonProperty(required = true)
//    @NotBlank
    private String zipCode;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.recipientCode}")
    private String recipientCode;
}
