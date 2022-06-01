package it.pagopa.selfcare.dashboard.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UpdateUserDto {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}")
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}")
    private String surname;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.institutionalEmail}")
    private String email;
}
