package it.pagopa.selfcare.dashboard.web.model.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class CertifiedFieldResource<T> {

    @ApiModelProperty(value = "${swagger.model.certifiedField.certified}", required = true)
    @NotNull
    private boolean certified;

    @ApiModelProperty(value = "${swagger.model.certifiedField.value}", required = true)
    @NotNull
    private T value;

}
