package it.pagopa.selfcare.dashboard.web.model.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductOnBoardingStatus;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Data
public class ProductsResource {
    @ApiModelProperty(value = "${swagger.dashboard.products.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.logo}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String logo;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.logoBgColor}")
    @Pattern(regexp = "^#[0-9A-F]{6}$")
    private String logoBgColor;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.imageUrl}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String imageUrl;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.title}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String title;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.description}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String description;

    @ApiModelProperty("${swagger.dashboard.products.model.urlPublic}")
    private String urlPublic;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.urlBO}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String urlBO;

    @ApiModelProperty("${swagger.dashboard.products.model.activatedAt}")
    private OffsetDateTime activatedAt;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.authorized}", required = true)
    @JsonProperty(required = true)
    private boolean authorized;

    @ApiModelProperty(value = "${swagger.dashboard.model.userRole}")
    private String userRole;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.productOnBoardingStatus}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private ProductOnBoardingStatus productOnBoardingStatus;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.children}")
    private List<SubProductResource> children;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.backOfficeEnvironmentConfigurations}")
    @Valid
    private Collection<BackOfficeConfigurationsResource> backOfficeEnvironmentConfigurations;

}
