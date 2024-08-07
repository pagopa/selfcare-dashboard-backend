package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.*;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInstitution;
import it.pagopa.selfcare.dashboard.core.exception.InvalidMemberListException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidUserGroupException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.SUSPENDED;
import static it.pagopa.selfcare.dashboard.connector.model.user.User.Fields.familyName;
import static it.pagopa.selfcare.dashboard.connector.model.user.User.Fields.name;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserGroupV2ServiceImpl implements UserGroupV2Service{

    private static final EnumSet<User.Fields> FIELD_LIST = EnumSet.of(name, familyName);

    private final UserGroupConnector groupConnector;
    private final UserApiConnector userApiConnector;

    static final String REQUIRED_GROUP_ID_MESSAGE = "A user group id is required";

    @Override
    public String createUserGroup(CreateUserGroup group) {
        log.trace("createUserGroup start");
        log.debug("createUserGroup group = {}", group);
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(group.getProductId());
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));

        List<String> retrievedId = retrieveIds(group.getInstitutionId(), userInfoFilter);

        if (group.getMembers().stream()
                .filter(uuid -> Collections.binarySearch(retrievedId, uuid) >= 0)
                .count() != group.getMembers().size()) {
            throw new InvalidMemberListException("Some members in the list aren't allowed for this institution");
        }
        String groupId = groupConnector.createUserGroup(group);
        log.debug("createUserGroup result = {}", groupId);
        log.trace("createUserGroup end");
        return groupId;
    }

    @Override
    public void delete(String groupId) {
        log.trace("delete start");
        log.debug("delete groupId = {}", groupId);
        Assert.hasText(groupId, REQUIRED_GROUP_ID_MESSAGE);
        groupConnector.delete(groupId);
        log.trace("delete end");
    }

    @Override
    public void activate(String groupId) {
        log.trace("activate start");
        log.debug("activate groupId = {}", groupId);
        Assert.hasText(groupId, REQUIRED_GROUP_ID_MESSAGE);
        groupConnector.activate(groupId);
        log.trace("activate end");
    }

    private List<String> retrieveIds(String institutionId, UserInfo.UserInfoFilter userInfoFilter) {
        List<String> retrievedUsers = userApiConnector.retrieveFilteredUserInstitution(
                institutionId,
                userInfoFilter);
        return retrievedUsers.stream()
                .sorted()
                .toList();
    }

    @Override
    public void suspend(String groupId) {
        log.trace("suspend start");
        log.debug("suspend groupId = {}", groupId);
        Assert.hasText(groupId, REQUIRED_GROUP_ID_MESSAGE);
        groupConnector.suspend(groupId);
        log.trace("suspend end");
    }

    @Override
    public void updateUserGroup(String groupId, UpdateUserGroup group) {
        log.trace("updateUserGroup start");
        log.debug("updateUserGroup groupId = {}, group = {}", groupId, group);
        UserGroupInfo userGroupInfo = groupConnector.getUserGroupById(groupId);

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(userGroupInfo.getProductId());
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));

        List<String> retrievedId = retrieveIds(userGroupInfo.getInstitutionId(), userInfoFilter);

        if (group.getMembers().stream()
                .filter(uuid -> Collections.binarySearch(retrievedId, uuid) >= 0)
                .count() != group.getMembers().size()) {
            throw new InvalidMemberListException("Some members in the list aren't allowed for this institution");
        }
        groupConnector.updateUserGroup(groupId, group);
        log.trace("updateUserGroup end");
    }

    @Override
    public void addMemberToUserGroup(String groupId, UUID userId) {
        log.trace("addMemberToUserGroup start");
        log.debug("addMemberToUserGroup groupId = {}, userId = {}", groupId, userId);
        Assert.hasText(groupId, REQUIRED_GROUP_ID_MESSAGE);
        Assert.notNull(userId, "A userId is required");
        UserGroupInfo retrievedGroup = groupConnector.getUserGroupById(groupId);
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(retrievedGroup.getProductId());
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));
        List<String> retrievedIds = retrieveIds(retrievedGroup.getInstitutionId(), userInfoFilter);
        if (!retrievedIds.contains(userId.toString())) {
            throw new InvalidMemberListException("This user is not allowed for this group");
        }
        groupConnector.addMemberToUserGroup(groupId, userId);
        log.trace("addMemberToUserGroup end");
    }

    @Override
    public void deleteMemberFromUserGroup(String groupId, UUID userId) {
        log.trace("deleteMemberFromUserGroup start");
        log.debug("deleteMemberFromUserGroup groupId = {}, userId = {}", groupId, userId);
        Assert.hasText(groupId, REQUIRED_GROUP_ID_MESSAGE);
        Assert.notNull(userId, "A userId is required");
        groupConnector.deleteMemberFromUserGroup(groupId, userId);
        log.trace("deleteMemberFromUserGroup end");
    }

    @Override
    public UserGroupInfo getUserGroupById(String groupId, String institutionId) {
        log.trace("getUserGroupById start");
        log.debug("getUserGroupById groupId = {}", groupId);
        Assert.hasText(groupId, REQUIRED_GROUP_ID_MESSAGE);
        UserGroupInfo userGroupInfo = groupConnector.getUserGroupById(groupId);

        Optional.ofNullable(institutionId).ifPresent(value -> {
            if (!value.equalsIgnoreCase(userGroupInfo.getInstitutionId())) {
                throw new InvalidUserGroupException("Could not find a UserGroup for given institutionId");
            }
        });

        List<UserInfo> members = new ArrayList<>();

        userGroupInfo.getMembers().forEach(user -> {
            UserInfo member = userApiConnector.getUserByUserIdInstitutionIdAndProductAndStates(user.getId(), institutionId, userGroupInfo.getProductId(), List.of(ACTIVE.name(), SUSPENDED.name()));
            members.add(member);
        });

        userGroupInfo.setMembers(members);

        User createdBy = userApiConnector.getUserById(userGroupInfo.getCreatedBy().getId(), institutionId, FIELD_LIST.stream().map(Enum::name).toList());
        userGroupInfo.setCreatedBy(createdBy);
        if (userGroupInfo.getModifiedBy() != null) {
            User modifiedBy = userApiConnector.getUserById(userGroupInfo.getModifiedBy().getId(), institutionId, FIELD_LIST.stream().map(Enum::name).toList());
            userGroupInfo.setModifiedBy(modifiedBy);
        }
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserGroupById userGroupInfo = {}", userGroupInfo);
        log.trace("getUserGroupById end");
        return userGroupInfo;
    }

    @Override
    public Page<UserGroup> getUserGroups(String institutionId, String productId, UUID userId, Pageable pageable) {
        log.trace("getUserGroups start");
        log.debug("getUserGroups institutionId = {}, productId = {}, userId = {}, pageable = {}", institutionId, productId, userId, pageable);
        UserGroupFilter userGroupFilter = new UserGroupFilter();
        userGroupFilter.setInstitutionId(Optional.ofNullable(institutionId));
        userGroupFilter.setUserId(Optional.ofNullable(userId));
        userGroupFilter.setProductId(Optional.ofNullable(productId));
        Page<UserGroup> groupInfos = groupConnector.getUserGroups(userGroupFilter, pageable);
        log.debug("getUserGroups result = {}", groupInfos);
        log.trace("getUserGroups end");

        return groupInfos;
    }

    @Override
    public void deleteMembersByUserId(String userId, String institutionId, String productId) {
        log.trace("deleteMembersByUserId start");
        log.debug("deleteMembersByUserId userId = {}", userId);
        List<UserInstitution> userInstitutionList = userApiConnector.retrieveFilteredUser(userId, institutionId, productId);
        if (CollectionUtils.isEmpty(userInstitutionList)) {
            log.debug("User not found, deleting members for userId = {}", userId);
            groupConnector.deleteMembers(userId, institutionId, productId);
        } else {
            log.debug("User found, not deleting members for userId = {}", userId);
        }
    }
}
