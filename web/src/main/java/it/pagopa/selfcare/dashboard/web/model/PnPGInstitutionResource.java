package it.pagopa.selfcare.dashboard.web.model;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionType;
import lombok.Data;

import java.util.List;

@Data
public class PnPGInstitutionResource {

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.id}")
//    @NotBlank
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.externalId}")
//    @NotBlank
    private String externalId;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.originId}")
//    @NotBlank
    private String originId;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.origin}")
//    @NotBlank
    private String origin;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.institutionType}")
    private InstitutionType institutionType;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.name}")
//    @NotBlank
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.category}")
    private String category;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.fiscalCode}")
//    @NotBlank
    private String fiscalCode;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.mailAddress}")
//    @NotBlank
    private String mailAddress;

    @ApiModelProperty(value = "${swagger.dashboard.model.userRole}")
//    @NotBlank
    private String userRole;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.status}")
//    @NotBlank
    private String status;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.address}")
//    @NotBlank
    private String address;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.zipCode}")
//    @NotBlank
    private String zipCode;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.recipientCode}")
    private String recipientCode;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.geographicTaxonomy}")
//    @NotNull
    private List<GeographicTaxonomyResource> geographicTaxonomies;
}
