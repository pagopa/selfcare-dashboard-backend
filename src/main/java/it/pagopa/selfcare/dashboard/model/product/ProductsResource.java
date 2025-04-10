package it.pagopa.selfcare.dashboard.model.product;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.product.entity.ProductStatus;
import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
public class ProductsResource {
    @ApiModelProperty(value = "${swagger.dashboard.products.model.id}")
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.logo}")
    private String logo;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.logoBgColor}")
    private String logoBgColor;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.imageUrl}")
    private String imageUrl;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.title}")
    private String title;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.description}")
    private String description;

    @ApiModelProperty("${swagger.dashboard.products.model.urlPublic}")
    private String urlPublic;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.urlBO}")
    private String urlBO;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.status}")
    private ProductStatus status;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.delegable}")
    private boolean delegable;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.children}")
    private List<SubProductResource> children;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.backOfficeEnvironmentConfigurations}")
    private Collection<BackOfficeConfigurationsResource> backOfficeEnvironmentConfigurations;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.invoiceable}")
    private boolean invoiceable;

}
