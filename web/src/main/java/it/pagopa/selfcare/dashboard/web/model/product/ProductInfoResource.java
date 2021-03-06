package it.pagopa.selfcare.dashboard.web.model.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ProductInfoResource {
    @ApiModelProperty(value = "${swagger.dashboard.products.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.title}")
    private String title;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.roleInfos}", required = true)
    @JsonProperty(required = true)
    @NotNull
    @Valid
    private List<ProductRoleInfoResource> roleInfos;
}
