package it.pagopa.selfcare.dashboard.web.model.user_groups;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupStatus;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserGroupPlainResource {
    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.institutionId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String institutionId;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.productId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String productId;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.description}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String description;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.status}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private UserGroupStatus status;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.membersCount}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private Integer membersCount;

}
