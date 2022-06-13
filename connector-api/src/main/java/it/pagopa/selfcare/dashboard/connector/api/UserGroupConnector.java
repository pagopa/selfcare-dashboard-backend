package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupFilter;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.UUID;

public interface UserGroupConnector {
    String createUserGroup(CreateUserGroup userGroupDto);

    void delete(String groupId);

    void deleteMembers(String memberId, String institutionId, String productId);

    void activate(String groupId);

    void suspend(String groupId);

    void updateUserGroup(String id, UpdateUserGroup userGroup);

    UserGroupInfo getUserGroupById(String id);

    void addMemberToUserGroup(String id, UUID userId);

    void deleteMemberFromUserGroup(String id, UUID userId);

    Collection<UserGroupInfo> getUserGroups(UserGroupFilter filter, Pageable pageable);
}
