package it.pagopa.selfcare.dashboard.web.model.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductOnBoardingStatus;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductStatus;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class SubProductResource {

    @ApiModelProperty(value = "${swagger.dashboard.products.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.title}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String title;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.productOnBoardingStatus}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private ProductOnBoardingStatus productOnBoardingStatus;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.status}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private ProductStatus status;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.imageUrl}")
    private String imageUrl;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.logo}")
    private String logo;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.logoBgColor}")
    @Pattern(regexp = "^#[0-9A-F]{6}$")
    private String logoBgColor;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.description}")
    private String description;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.urlPublic}")
    private String urlPublic;
}
