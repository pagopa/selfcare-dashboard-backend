package it.pagopa.selfcare.dashboard.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class PnPGInstitutionResource {

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.id}")
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.externalId}")
    private String externalId;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.originId}")
    private String originId;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.origin}")
    private String origin;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.institutionType}")
    private String institutionType;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.name}")
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.category}")
    private String category;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.fiscalCode}")
    private String fiscalCode;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.mailAddress}")
    private String mailAddress;

    @ApiModelProperty(value = "${swagger.dashboard.model.userRole}")
    private String userRole;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.status}")
    private String status;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.address}")
    private String address;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.zipCode}")
    private String zipCode;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.recipientCode}")
    private String recipientCode;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.geographicTaxonomy}")
    private List<GeographicTaxonomyResource> geographicTaxonomies;
}
