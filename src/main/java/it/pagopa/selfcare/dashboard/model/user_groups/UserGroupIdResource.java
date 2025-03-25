package it.pagopa.selfcare.dashboard.model.user_groups;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserGroupIdResource {

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.id}", required = true)
    private String id;
}
