package it.pagopa.selfcare.dashboard.web.model.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ProductRoleMappingsResource {

    @ApiModelProperty(value = "${swagger.dashboard.product-role-mappings.model.partyRole}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private PartyRole partyRole;

    @ApiModelProperty(value = "${swagger.dashboard.product-role-mappings.model.selcRole}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private SelfCareAuthority selcRole;

    @ApiModelProperty(value = "${swagger.dashboard.product-role-mappings.model.multiroleAllowed}", required = true)
    private boolean multiroleAllowed;

    @ApiModelProperty(value = "${swagger.dashboard.product-role-mappings.model.productRoles}", required = true)
    @JsonProperty(required = true)
    @NotEmpty
    @Valid
    private List<ProductRoleResource> productRoles;


    @Data
    @EqualsAndHashCode(of = "code")
    public static class ProductRoleResource {

        @ApiModelProperty(value = "${swagger.dashboard.product-role.model.code}", required = true)
        @JsonProperty(required = true)
        @NotBlank
        private String code;

        @ApiModelProperty(value = "${swagger.dashboard.product-role.model.label}", required = true)
        @JsonProperty(required = true)
        @NotBlank
        private String label;

        @ApiModelProperty(value = "${swagger.dashboard.product-role.model.description}", required = true)
        @JsonProperty(required = true)
        @NotBlank
        private String description;
    }

}
