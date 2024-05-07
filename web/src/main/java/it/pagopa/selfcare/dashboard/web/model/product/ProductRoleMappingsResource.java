package it.pagopa.selfcare.dashboard.web.model.product;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
public class ProductRoleMappingsResource {

    @ApiModelProperty(value = "${swagger.dashboard.product-role-mappings.model.partyRole}")
    private PartyRole partyRole;

    @ApiModelProperty(value = "${swagger.dashboard.product-role-mappings.model.selcRole}")
    private SelfCareAuthority selcRole;

    @ApiModelProperty(value = "${swagger.dashboard.product-role-mappings.model.multiroleAllowed}")
    private boolean multiroleAllowed;

    @ApiModelProperty(value = "${swagger.dashboard.product-role-mappings.model.productRoles}")
    private List<ProductRoleResource> productRoles;


    @Data
    @EqualsAndHashCode(of = "code")
    public static class ProductRoleResource {

        @ApiModelProperty(value = "${swagger.dashboard.product-role.model.code}")
        private String code;

        @ApiModelProperty(value = "${swagger.dashboard.product-role.model.label}")
        private String label;

        @ApiModelProperty(value = "${swagger.dashboard.product-role.model.description}")
        private String description;
        
    }

}
