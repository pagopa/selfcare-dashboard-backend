package it.pagopa.selfcare.dashboard.model.user_groups;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.dashboard.model.groups.UserGroupStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserGroupPlainResource {
    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.id}")
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.institutionId}")
    private String institutionId;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.productId}")
    private String productId;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.name}")
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.description}")
    private String description;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.status}")
    private UserGroupStatus status;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.membersCount}")
    private Integer membersCount;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.createdAt}")
    private Instant createdAt;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.createdBy}")
    private UUID createdBy;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.modifiedAt}")
    private Instant modifiedAt;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.modifiedBy}")
    private UUID modifiedBy;

}
