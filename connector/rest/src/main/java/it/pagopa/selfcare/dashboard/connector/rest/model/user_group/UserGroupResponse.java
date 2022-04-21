package it.pagopa.selfcare.dashboard.connector.rest.model.user_group;

import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupStatus;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UserGroupResponse {

    private String id;
    private String institutionId;
    private String productId;
    private String name;
    private String description;
    private UserGroupStatus status;
    private List<String> members;
    private Instant createdAt;
    private String createdBy;
    private Instant modifiedAt;
    private String modifiedBy;

}
