package it.pagopa.selfcare.dashboard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SearchUserDto {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.fiscalCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank(message = "Fiscal code is required")
    private String fiscalCode;

}
