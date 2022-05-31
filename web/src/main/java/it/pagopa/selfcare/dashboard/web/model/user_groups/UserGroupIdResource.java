package it.pagopa.selfcare.dashboard.web.model.user_groups;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserGroupIdResource {
    @NotBlank
    private String id;
}
