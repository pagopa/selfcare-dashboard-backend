package it.pagopa.selfcare.dashboard.model.product;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProductInfoResource {
    @ApiModelProperty(value = "${swagger.dashboard.products.model.id}")
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.title}")
    private String title;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.roleInfos}")
    private List<ProductRoleInfoResource> roleInfos;
}
