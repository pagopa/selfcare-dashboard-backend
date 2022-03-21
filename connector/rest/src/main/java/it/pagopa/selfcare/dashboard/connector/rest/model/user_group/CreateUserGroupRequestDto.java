package it.pagopa.selfcare.dashboard.connector.rest.model.user_group;

import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupStatus;
import lombok.Data;

import java.util.List;

@Data
public class CreateUserGroupRequestDto {
    private String institutionId;
    private String productId;
    private String name;
    private String description;
    private UserGroupStatus status;
    List<String> members;
}
