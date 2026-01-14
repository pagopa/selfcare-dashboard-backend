package it.pagopa.selfcare.dashboard.model.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CheckAttachmentResponse {

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.isAttachmentAvailable}")
    private final Boolean isAttachmentAvailable;

    @JsonCreator
    public CheckAttachmentResponse(@JsonProperty("isAttachmentAvailable") Boolean isAttachmentAvailable) {
        this.isAttachmentAvailable = isAttachmentAvailable;
    }
}

