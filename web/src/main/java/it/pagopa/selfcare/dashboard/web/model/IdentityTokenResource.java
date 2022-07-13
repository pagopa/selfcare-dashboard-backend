package it.pagopa.selfcare.dashboard.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
public class IdentityTokenResource {

    @ApiModelProperty(value = "${swagger.dashboard.token.model.token}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String token;

}
