package it.pagopa.selfcare.dashboard.web.model.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class UserIdResource {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.id}", required = true)
    @NotNull
    private UUID id;
}
