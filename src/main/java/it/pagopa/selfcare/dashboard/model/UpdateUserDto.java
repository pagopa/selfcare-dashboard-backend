package it.pagopa.selfcare.dashboard.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Data
public class UpdateUserDto {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}")
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}")
    private String surname;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.institutionalEmail}")
    @Email
    private String email;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.institutionalPhone}")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Il numero di telefono non è valido")
    private String mobilePhone;
}
