package it.pagopa.selfcare.dashboard.web.model.user_groups;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserGroupIdResource {

    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;
}
