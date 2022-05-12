package it.pagopa.selfcare.dashboard.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateUserDto {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.fiscalCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String fiscalCode;//FIXME
    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String name;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String surname;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.institutionalEmail}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String email;
}
