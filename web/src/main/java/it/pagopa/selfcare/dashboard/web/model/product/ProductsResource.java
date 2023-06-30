package it.pagopa.selfcare.dashboard.web.model.product;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductOnBoardingStatus;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductStatus;
import lombok.Data;

import java.time.OffsetDateTime;
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

    @ApiModelProperty("${swagger.dashboard.products.model.activatedAt}")
    private OffsetDateTime activatedAt;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.authorized}")
    private boolean authorized;

    @ApiModelProperty(value = "${swagger.dashboard.model.userRole}")
    private String userRole;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.productOnBoardingStatus}")
    private ProductOnBoardingStatus productOnBoardingStatus;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.status}")
    private ProductStatus status;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.children}")
    private List<SubProductResource> children;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.backOfficeEnvironmentConfigurations}")
    private Collection<BackOfficeConfigurationsResource> backOfficeEnvironmentConfigurations;

}
