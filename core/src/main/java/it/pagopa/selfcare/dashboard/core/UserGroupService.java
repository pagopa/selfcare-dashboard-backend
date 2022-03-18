package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;

import java.util.Optional;
import java.util.UUID;

public interface UserGroupService {
    void createUserGroup(CreateUserGroup group);

    void delete(String groupId);

    void activate(String groupId);

    void suspend(String groupId);

    void updateUserGroup(String groupId, UpdateUserGroup group);

    void addMemberToUserGroup(String groupId, UUID userId);

    void deleteMemberFromUserGroup(String groupId, UUID userId);

    UserGroupInfo getUserGroupById(String groupId, Optional<String> institutionId);
}
