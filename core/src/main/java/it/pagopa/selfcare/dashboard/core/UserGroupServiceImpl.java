package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupFilter;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.exception.InvalidMemberListException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidUserGroupException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.SUSPENDED;
import static it.pagopa.selfcare.dashboard.connector.model.user.User.Fields.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserGroupServiceImpl implements UserGroupService {

    private static final EnumSet<User.Fields> MEMBER_FIELD_LIST = EnumSet.of(name, familyName, workContacts);
    private static final EnumSet<User.Fields> FIELD_LIST = EnumSet.of(name, familyName);

    static final String REQUIRED_GROUP_ID_MESSAGE = "A user group id is required";

    private final UserGroupConnector groupConnector;
    private final UserRegistryConnector userRegistryConnector;
    private final MsCoreConnector msCoreConnector;

    @Override
    public String createUserGroup(CreateUserGroup group) {
        log.trace("createUserGroup start");
        log.debug("createUserGroup group = {}", group);
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(group.getProductId());
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));

        List<String> retrievedId = retrievedIds(group.getInstitutionId(), userInfoFilter);

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

    private List<String> retrievedIds(String institutionId, UserInfo.UserInfoFilter userInfoFilter) {
        Collection<UserInfo> retrievedUsers = msCoreConnector.getUsers(
                institutionId,
                userInfoFilter);
        return retrievedUsers.stream()
                .map(UserInfo::getId)
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

        List<String> retrievedId = retrievedIds(userGroupInfo.getInstitutionId(), userInfoFilter);

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
        List<String> retrievedIds = retrievedIds(retrievedGroup.getInstitutionId(), userInfoFilter);
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
    public UserGroupInfo getUserGroupById(String groupId, Optional<String> institutionId) {
        log.trace("getUserGroupById start");
        log.debug("getUserGroupById groupId = {}", groupId);
        Assert.hasText(groupId, REQUIRED_GROUP_ID_MESSAGE);
        Assert.notNull(institutionId, "An optional of institutionId is required");
        UserGroupInfo userGroupInfo = groupConnector.getUserGroupById(groupId);
        institutionId.ifPresent(value -> {
            if (!value.equalsIgnoreCase(userGroupInfo.getInstitutionId())) {
                throw new InvalidUserGroupException("Could not find a UserGroup for given institutionId");
            }
        });
        Comparator<UserInfo> userInfoComparator = Comparator.comparing(UserInfo::getId);

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(userGroupInfo.getProductId());
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));
        List<UserInfo> userInfos = msCoreConnector.getUsers(userGroupInfo.getInstitutionId(), userInfoFilter).stream()
                .sorted(userInfoComparator)
                .toList();
        userGroupInfo.setMembers(userGroupInfo.getMembers().stream()
                .map(userInfo -> {
                    int index = Collections.binarySearch(userInfos, userInfo, userInfoComparator);
                    if (index < 0) {
                        log.error(String.format("Member with uuid %s has no relationship with institution id '%s' and product id '%s'",
                                userInfo.getId(),
                                userGroupInfo.getInstitutionId(),
                                userGroupInfo.getProductId()));
                        return null;
                    }
                    userInfos.get(index).setUser(userRegistryConnector.getUserByInternalId(userInfo.getId(), MEMBER_FIELD_LIST));
                    return userInfos.get(index);
                }).filter(Objects::nonNull)
                .toList());
        User createdBy = userRegistryConnector.getUserByInternalId(userGroupInfo.getCreatedBy().getId(), FIELD_LIST);
        userGroupInfo.setCreatedBy(createdBy);
        if (userGroupInfo.getModifiedBy() != null) {
            User modifiedBy = userRegistryConnector.getUserByInternalId(userGroupInfo.getModifiedBy().getId(), FIELD_LIST);
            userGroupInfo.setModifiedBy(modifiedBy);
        }
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserGroupById userGroupInfo = {}", userGroupInfo);
        log.trace("getUserGroupById end");
        return userGroupInfo;
    }

    @Override
    public Page<UserGroupInfo> getUserGroups(Optional<String> institutionId, Optional<String> productId, Optional<UUID> userId, Pageable pageable) {
        log.trace("getUserGroups start");
        log.debug("getUserGroups institutionId = {}, productId = {}, userId = {}, pageable = {}", institutionId, productId, userId, pageable);
        Assert.notNull(institutionId, "An optional institutionId is required");
        Assert.notNull(productId, "An optional productId is required");
        Assert.notNull(userId, "An optional userId is required");
        UserGroupFilter userGroupFilter = new UserGroupFilter();
        userGroupFilter.setInstitutionId(institutionId);
        userGroupFilter.setUserId(userId);
        userGroupFilter.setProductId(productId);
        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(userGroupFilter, pageable);
        log.debug("getUserGroups result = {}", groupInfos);
        log.trace("getUserGroups end");

        return groupInfos;
    }

    @Override
    @Async
    public void deleteMembersByRelationshipId(String relationshipId) {
        log.trace("deleteMembers start");
        log.debug("deleteMembers relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, "A relationshipId is required");
        UserInfo user = msCoreConnector.getUser(relationshipId);
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        String productId = user.getProducts().keySet().iterator().next();
        Assert.notNull(productId, "A product Id is required");
        String institutionId = user.getInstitutionId();
        Assert.notNull(institutionId, "An institution id is required");
        String userId = user.getId();
        Assert.notNull(userId, "A user id is required");
        userInfoFilter.setProductId(productId);
        userInfoFilter.setUserId(user.getId());
        Collection<UserInfo> users = msCoreConnector.getUsers(user.getInstitutionId(), userInfoFilter);
        if (users.isEmpty()) {
            groupConnector.deleteMembers(userId, institutionId, productId);
        }
        log.trace("deleteMembers end");
    }

}
