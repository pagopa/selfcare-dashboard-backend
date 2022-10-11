package it.pagopa.selfcare.dashboard.web.model.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BackOfficeConfigurationsResource {

    @ApiModelProperty(value = "${swagger.dashboard.product-backoffice-configurations.model.environment}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String environment;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.urlBO}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String url;

}
