package it.pagopa.selfcare.dashboard.connector.model.groups;

import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserResource;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UserGroupInfo {
    private String id;
    private String institutionId;
    private String productId;
    private String description;
    private String name;
    private UserGroupStatus status;
    private List<UserInfo> members;
    private Instant createdAt;
    private UserResource createdBy;
    private Instant modifiedAt;
    private UserResource modifiedBy;
}
