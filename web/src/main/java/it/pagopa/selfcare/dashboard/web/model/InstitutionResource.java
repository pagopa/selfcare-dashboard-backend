package it.pagopa.selfcare.dashboard.web.model;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import lombok.Data;

import java.util.List;

@Data
public class InstitutionResource {

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

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.pspData.vatNumberGroup}")
    private Boolean vatNumberGroup;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.supportContact}")
    private SupportContactResource supportContact;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.vatNumber}")
    private String vatNumber;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.subunitCode}")
    private String subunitCode;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.subunitType}")
    private String subunitType;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.aooParentCode}")
    private String aooParentCode;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.parentDescription}")
    private String parentDescription;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.products}")
    private List<OnboardedProductResource> products;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.city}")
    private String city;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.country}")
    private String country;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.county}")
    private String county;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.delegation}")
    private boolean delegation;

}
