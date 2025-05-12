package it.pagopa.selfcare.dashboard.model.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OnboardingInfo {

    @ApiModelProperty(value = "${swagger.dashboard.products.model.id}")
    private String productId;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.status}")
    private String status;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.contract.available}")
    private Boolean contractAvailable;

}
