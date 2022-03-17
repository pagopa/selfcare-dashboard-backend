package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;

public interface UserGroupConnector {
    void createUserGroup(CreateUserGroup userGroupDto);

    void delete(String groupId);

    void activate(String groupId);

    void suspend(String groupId);

    void updateUserGroup(String id, UpdateUserGroup userGroup);

    UserGroupInfo getUserGroupById(String id);

//    Collection<UserGroupInfo> getUserGroups();
}
