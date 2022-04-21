package it.pagopa.selfcare.dashboard.web.model.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@FieldNameConstants(asEnum = true)
public class UserResource {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.id}", required = true)
    @NotNull
    private UUID id;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}", required = true)
    private CertifiableFieldResource<String> name;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}", required = true)
    private CertifiableFieldResource<String> familyName;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.email}", required = true)
    private CertifiableFieldResource<String> email;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.fiscalCode}", required = true)
    private String fiscalCode;
}
