package it.pagopa.selfcare.dashboard.web.model.user_groups;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupStatus;
import it.pagopa.selfcare.dashboard.web.model.product.ProductUserResource;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UserGroupResource {
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

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.members}")
    private List<ProductUserResource> members;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.createdAt}")
    private Instant createdAt;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.createdBy}")
    private PlainUserResource createdBy;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.modifiedAt}")
    private Instant modifiedAt;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.modifiedBy}")
    private PlainUserResource modifiedBy;

}
