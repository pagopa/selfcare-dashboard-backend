package it.pagopa.selfcare.dashboard.web.model.user_groups;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupStatus;
import it.pagopa.selfcare.dashboard.web.model.product.ProductUserResource;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UserGroupResource {
    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.id}", required = true)
    @JsonProperty(required = true)
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.institutionId}", required = true)
    @JsonProperty(required = true)
    private String institutionId;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.productId}", required = true)
    @JsonProperty(required = true)
    private String productId;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.name}", required = true)
    @JsonProperty(required = true)
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.description}", required = true)
    @JsonProperty(required = true)
    private String description;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.status}", required = true)
    @JsonProperty(required = true)
    private UserGroupStatus status;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.members}", required = true)
    @JsonProperty()
    private List<ProductUserResource> members;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.createdAt}")
    @JsonProperty
    private Instant createdAt;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.createdBy}")
    @JsonProperty
    private PlainUserResource createdBy;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.modifiedAt}")
    @JsonProperty
    private Instant modifiedAt;

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.modifiedBy}")
    @JsonProperty
    private PlainUserResource modifiedBy;

}
