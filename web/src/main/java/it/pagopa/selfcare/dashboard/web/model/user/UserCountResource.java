package it.pagopa.selfcare.dashboard.web.model.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class UserCountResource {

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.id}")
    private String institutionId;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.id}")
    private String productId;

    @ApiModelProperty(value = "${swagger.dashboard.product-role-mappings.model.partyRole}")
    private List<String> roles;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.status}")
    private List<String> status;

    @ApiModelProperty(value = "${swagger.dashboard.products.model.users}")
    private Long count;

}
