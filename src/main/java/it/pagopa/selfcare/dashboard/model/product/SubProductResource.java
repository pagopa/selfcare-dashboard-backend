package it.pagopa.selfcare.dashboard.model.product;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.product.entity.ProductStatus;
import lombok.Data;

@Data
public class SubProductResource {

    @ApiModelProperty(value = "${swagger.dashboard.products.model.id}")
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.title}")
    private String title;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.productOnBoardingStatus}")
    private ProductOnBoardingStatus productOnBoardingStatus;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.status}")
    private ProductStatus status;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.delegable}")
    private boolean delegable;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.imageUrl}")
    private String imageUrl;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.logo}")
    private String logo;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.logoBgColor}")
    private String logoBgColor;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.description}")
    private String description;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.urlPublic}")
    private String urlPublic;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.invoiceable}")
    private boolean invoiceable;

}
