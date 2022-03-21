package it.pagopa.selfcare.dashboard.web.model.user_groups;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserGroupMemberResource {
    @ApiModelProperty(value = "${swagger.dashboard.user.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String name;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String surname;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.email}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String email;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.productRole}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String role;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.role}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private SelfCareAuthority selcRole;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.status}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String status;

}
