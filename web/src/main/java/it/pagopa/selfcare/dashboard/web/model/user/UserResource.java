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
    @FieldNameConstants.Exclude
    private UUID id;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}")
    private CertifiableFieldBooleanResource<String> name;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}")
    private CertifiableFieldBooleanResource<String> familyName;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.institutionEmail}")
    private CertifiableFieldBooleanResource<String> email;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.fiscalCode}")
    private String fiscalCode;
}
