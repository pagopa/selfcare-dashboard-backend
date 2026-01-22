package it.pagopa.selfcare.dashboard.model.delegation;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class DelegationRequestDto {

    @ApiModelProperty(value = "${swagger.dashboard.delegation.model.from}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String from;

    @ApiModelProperty(value = "${swagger.dashboard.delegation.model.to}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String to;

    @ApiModelProperty(value = "${swagger.dashboard.delegation.model.productId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String productId;

    @ApiModelProperty(value = "${swagger.dashboard.delegation.model.type}", required = true)
    @JsonProperty(required = true)
    @DelegationTypeSubset(anyOf = {DelegationType.AOO, DelegationType.PT})
    private DelegationType type;

    @ApiModelProperty(value = "${swagger.dashboard.delegation.model.institutionFromName}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String institutionFromName;

    @ApiModelProperty(value = "${swagger.dashboard.delegation.model.institutionToName}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String institutionToName;

}
