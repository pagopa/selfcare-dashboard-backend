package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
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

    Collection<UserGroupInfo> getUserGroups(Optional<String> institutionId, Optional<String> productId, Optional<UUID> userId, Pageable pageable);

    void deleteMembers(String memberId, String institutionId, String productId);

}
