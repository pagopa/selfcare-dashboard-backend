package it.pagopa.selfcare.dashboard.web.model.user_groups;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class PlainUserResource {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.id}")
    private UUID id;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}")
    private String name;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}")
    private String surname;

}
