package it.pagopa.selfcare.dashboard.model.user;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import lombok.Data;

@Data
public class OnboardingInfo {

    @ApiModelProperty(value = "${swagger.dashboard.products.model.id}")
    private String productId;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.status}")
    private String status;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.contract.available}")
    private Boolean contractAvailable;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.origin}")
    private String origin;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.originId}")
    private String originId;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.institutionType}")
    private InstitutionType institutionType;



}
