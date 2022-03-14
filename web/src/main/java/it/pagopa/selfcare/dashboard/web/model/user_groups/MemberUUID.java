package it.pagopa.selfcare.dashboard.web.model.user_groups;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class MemberUUID {
    @ApiModelProperty(value = "${swagger.dashboard.user-group.model.memberId}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private UUID member;
}
