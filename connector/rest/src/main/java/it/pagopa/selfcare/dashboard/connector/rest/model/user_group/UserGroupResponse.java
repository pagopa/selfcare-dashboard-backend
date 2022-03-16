package it.pagopa.selfcare.dashboard.connector.rest.model.user_group;

import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupStatus;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class UserGroupResponse {

    private String institutionId;
    private String productId;
    private String name;
    private String description;
    private UserGroupStatus status;
    private List<UUID> members;
    private Instant createdAt;
    private String createdBy;
    private Instant modifiedAt;
    private String modifiedBy;

}
