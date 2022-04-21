package it.pagopa.selfcare.dashboard.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserResource {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}", required = true)
    @JsonProperty(required = true)
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}", required = true)
    @JsonProperty(required = true)
    private String surname;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.certification}", required = true)
    @JsonProperty(required = true)
    private boolean certification;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.email}", required = true)
    @JsonProperty(required = true)
    private String email;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.fiscalCode}", required = true)
    @JsonProperty(required = true)
    private String fiscalCode;


}
