package it.pagopa.selfcare.dashboard.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
public class ProductsResource {
    @ApiModelProperty(value = "${swagger.dashboard.products.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.code}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String code;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.logo}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String logo;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.title}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String title;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.description}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String description;

    @ApiModelProperty("${swagger.dashboard.products.model.urlPublic}")
    @NotBlank
    private String urlPublic;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.urlBO}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String urlBO;

    @ApiModelProperty("${swagger.dashboard.products.model.creationDateTime}")
    @NotNull
    private OffsetDateTime creationDateTime;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.active}", required = true)
    @JsonProperty(required = true)
    private boolean active;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.authorized}", required = true)
    @JsonProperty(required = true)
    private boolean authorized;
}
