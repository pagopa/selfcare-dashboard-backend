package it.pagopa.selfcare.dashboard.web.model.user_groups;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class PlainUserResource {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.id}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private UUID id;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String name;
    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String surname;

}
