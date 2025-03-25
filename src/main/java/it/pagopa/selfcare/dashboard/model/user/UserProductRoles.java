package it.pagopa.selfcare.dashboard.model.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
public class UserProductRoles {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.role}")
    String role;

    @NotEmpty
    Set<String> productRoles;
}
