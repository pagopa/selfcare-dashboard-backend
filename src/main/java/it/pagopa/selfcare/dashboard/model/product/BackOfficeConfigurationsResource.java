package it.pagopa.selfcare.dashboard.model.product;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BackOfficeConfigurationsResource {

    @ApiModelProperty(value = "${swagger.dashboard.product-backoffice-configurations.model.environment}")
    private String environment;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.urlBO}")
    private String url;

}
