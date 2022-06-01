package it.pagopa.selfcare.dashboard.web.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class UserResource {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.id}", required = true)
    @NotNull
    private UUID id;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}")
    private CertifiedFieldResource<String> name;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}")
    private CertifiedFieldResource<String> familyName;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.institutionalEmail}")
    private CertifiedFieldResource<String> email;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.fiscalCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String fiscalCode;
}
