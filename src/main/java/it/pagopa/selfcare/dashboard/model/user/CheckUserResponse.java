package it.pagopa.selfcare.dashboard.model.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CheckUserResponse {

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.checkUserResponse}")
    private final Boolean isUserOnboarded;

    @JsonCreator
    public CheckUserResponse(@JsonProperty("isUserOnboarded") Boolean isUserOnboarded) {
        this.isUserOnboarded = isUserOnboarded;
    }
}

