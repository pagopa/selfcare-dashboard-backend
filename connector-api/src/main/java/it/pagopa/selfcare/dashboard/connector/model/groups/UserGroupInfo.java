package it.pagopa.selfcare.dashboard.connector.model.groups;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class UserGroupInfo {
    private String id;
    private String institutionId;
    private String productId;
    private String description;
    private String name;
    private UserGroupStatus status;
    private List<UUID> members;
    private Instant createdAt;
    private String createdBy;
    private Instant modifiedAt;
    private String modifiedBy;
}
