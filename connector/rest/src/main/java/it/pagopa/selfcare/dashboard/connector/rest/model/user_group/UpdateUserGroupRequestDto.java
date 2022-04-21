package it.pagopa.selfcare.dashboard.connector.rest.model.user_group;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserGroupRequestDto {
    private String name;
    private String description;
    private List<String> members;
}
