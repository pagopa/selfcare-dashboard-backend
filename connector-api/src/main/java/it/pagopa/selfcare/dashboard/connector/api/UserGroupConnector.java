package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;

public interface UserGroupConnector {
    void createUserGroup(CreateUserGroup userGroupDto);

    void delete(String groupId);

    void activate(String groupId);

}
