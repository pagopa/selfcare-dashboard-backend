package it.pagopa.selfcare.dashboard.web.model.product;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import lombok.Data;

@Data
public class ProductRoleInfoResource {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.relationshipId}")
    private String relationshipId;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.productRole}")
    private String role;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.status}")
    private String status;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.role}")
    private SelfCareAuthority selcRole;

}
