package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserGroupV2Service {
    void deleteMembersByUserId(String userId, String institutionId, String productId);

    String createUserGroup(CreateUserGroup group);

    void delete(String groupId);

    void activate(String groupId);

    void suspend(String groupId);

    void updateUserGroup(String groupId, UpdateUserGroup group);

    void addMemberToUserGroup(String groupId, UUID userId);

    void deleteMemberFromUserGroup(String groupId, UUID userId);

    UserGroupInfo getUserGroupById(String groupId, String institutionId);

    Page<UserGroupInfo> getUserGroups(String institutionId, String productId, UUID userId, Pageable pageable);
}
