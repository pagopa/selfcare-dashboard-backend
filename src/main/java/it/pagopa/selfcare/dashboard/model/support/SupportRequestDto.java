package it.pagopa.selfcare.dashboard.model.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.dashboard.validator.UrlEncoded;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class SupportRequestDto {

    @ApiModelProperty(value = "${swagger.dashboard.support.model.email}", required = true)
    @JsonProperty(required = true)
    @Email @NotBlank
    private String email;

    @ApiModelProperty(value = "${swagger.dashboard.support.model.productId}")
    private String productId;

    @ApiModelProperty(value = "${swagger.dashboard.support.model.userId}")
    private String userId;

    @ApiModelProperty(value = "${swagger.dashboard.support.model.institutionId}")
    private String institutionId;

    @ApiModelProperty(value = "${swagger.dashboard.support.model.data}")
    @UrlEncoded
    private String data;

}
