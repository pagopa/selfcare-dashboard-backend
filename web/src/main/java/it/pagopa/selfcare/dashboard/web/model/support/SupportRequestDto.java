package it.pagopa.selfcare.dashboard.web.model.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class SupportRequestDto {

    @ApiModelProperty(value = "${swagger.dashboard.support.model.email}", required = true)
    @JsonProperty(required = true)
    @Email @NotBlank
    private String email;

    @ApiModelProperty(value = "${swagger.dashboard.support.model.productId}", required = false)
    private String productId;

}
