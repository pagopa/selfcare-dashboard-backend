package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;

public interface UserGroupConnector {
    void createUserGroup(CreateUserGroup userGroupDto);
}
