package it.pagopa.selfcare.dashboard.web.model.user;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
public class UserProductRoles {
    @NotEmpty
    Set<String> productRoles;
}
