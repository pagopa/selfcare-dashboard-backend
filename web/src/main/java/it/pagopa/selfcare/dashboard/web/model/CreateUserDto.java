package it.pagopa.selfcare.dashboard.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
public class CreateUserDto {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}")
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}")
    private String surname;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.fiscalCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String taxCode;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.email}")
    @Email
    private String email;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.productRoles}", required = true)
    @JsonProperty(required = true)
    @NotEmpty
    private Set<String> productRoles;

}
