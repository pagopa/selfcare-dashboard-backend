package it.pagopa.selfcare.dashboard.web.model.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ProductRoleInfoResource {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.relationshipId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String relationshipId;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.productRole}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String role;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.status}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String status;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.role}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private SelfCareAuthority selcRole;
}
