package it.pagopa.selfcare.dashboard.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ProductsResource {
    @ApiModelProperty("${swagger.products.model.id}")
    private String id;
    @ApiModelProperty("${swagger.products.model.logo}")
    private String logo;
    @ApiModelProperty("${swagger.products.model.title}")
    private String title;
    @ApiModelProperty("${swagger.products.model.description}")
    private String description;
    @ApiModelProperty("${swagger.products.model.urlPublic}")
    private String urlPublic;
    @ApiModelProperty("${swagger.products.model.urlBO}")
    private String urlBO;
    @ApiModelProperty("${swagger.products.model.activationDateTime}")
    private OffsetDateTime activationDateTime;
    @ApiModelProperty("${swagger.products.model.active}")
    private boolean active;
    @ApiModelProperty("${swagger.products.model.authorized}")
    private boolean authorized;
}
