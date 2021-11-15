package it.pagopa.selfcare.dashboard.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ProductsResource {
    @ApiModelProperty(value = "${swagger.dashboard.products.model.id}", required = true)
    @JsonProperty(required = true)
    private String id;
    @ApiModelProperty(value = "${swagger.dashboard.products.model.code}", required = true)
    @JsonProperty(required = true)
    private String code;
    @ApiModelProperty("${swagger.dashboard.products.model.logo}")
    private String logo;
    @ApiModelProperty(value = "${swagger.dashboard.products.model.title}", required = true)
    @JsonProperty(required = true)
    private String title;
    @ApiModelProperty("${swagger.dashboard.products.model.description}")
    private String description;
    @ApiModelProperty("${swagger.dashboard.products.model.urlPublic}")
    private String urlPublic;
    @ApiModelProperty(value = "${swagger.dashboard.products.model.urlBO}", required = true)
    @JsonProperty(required = true)
    private String urlBO;
    @ApiModelProperty(value = "${swagger.dashboard.products.model.activationDateTime}", required = true)
    @JsonProperty(required = true)
    private OffsetDateTime activationDateTime;
    @ApiModelProperty(value = "${swagger.dashboard.products.model.active}", required = true)
    @JsonProperty(required = true)
    private boolean active;
    @ApiModelProperty(value = "${swagger.dashboard.products.model.authorized}", required = true)
    @JsonProperty(required = true)
    private boolean authorized;
}
