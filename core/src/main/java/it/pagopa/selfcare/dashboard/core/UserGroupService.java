package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;

public interface UserGroupService {
    void createUserGroup(CreateUserGroup group);

    void delete(String groupId);
}
