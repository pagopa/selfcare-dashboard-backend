package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupFilter;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.exception.InvalidMemberListException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidUserGroupException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupConnector groupConnector;
    private final UserRegistryConnector userRegistryConnector;
    private final PartyConnector partyConnector;
    static final String REQUIRED_GROUP_ID_MESSAGE = "A user group id is required";


    @Autowired
    public UserGroupServiceImpl(UserGroupConnector groupConnector, UserRegistryConnector userRegistryConnector, PartyConnector partyConnector) {
        this.groupConnector = groupConnector;
        this.userRegistryConnector = userRegistryConnector;
        this.partyConnector = partyConnector;
    }

    @Override
    public void createUserGroup(CreateUserGroup group) {
        log.trace("createUserGroup start");
        log.debug("createUserGroup group = {}", group);
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(Optional.of(group.getProductId()));
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(RelationshipState.ACTIVE)));

        List<String> retrievedId = retrievedIds(group.getInstitutionId(), userInfoFilter);

        if (group.getMembers().stream()
                .filter(uuid -> Collections.binarySearch(retrievedId, uuid) >= 0)
                .count() != group.getMembers().size()) {
            throw new InvalidMemberListException("Some members in the list aren't allowed for this institution");
        }
        groupConnector.createUserGroup(group);
        log.trace("createUserGroup end");

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

    private List<String> retrievedIds(String groupInstitution, UserInfo.UserInfoFilter userInfoFilter) {
        Collection<UserInfo> retrievedUsers = partyConnector.getUsers(
                groupInstitution,
                userInfoFilter);
        return retrievedUsers.stream()
                .map(UserInfo::getId)
                .sorted()
                .collect(Collectors.toList());
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
        userInfoFilter.setProductId(Optional.of(userGroupInfo.getProductId()));
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(RelationshipState.ACTIVE)));


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
        userInfoFilter.setProductId(Optional.of(userGroupInfo.getProductId()));
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(RelationshipState.ACTIVE)));
        List<UserInfo> userInfos = partyConnector.getUsers(
                userGroupInfo.getInstitutionId(),
                userInfoFilter
        )
                .stream()
                .sorted(userInfoComparator)
                .collect(Collectors.toList());
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
                    return userInfos.get(index);
                }).filter(Objects::nonNull)
                .collect(Collectors.toList()));
        if (userGroupInfo.getCreatedBy() != null) {
            User createdBy = userRegistryConnector.getUserByInternalId(userGroupInfo.getCreatedBy().getId());
            userGroupInfo.setCreatedBy(createdBy);
        }
        if (userGroupInfo.getModifiedBy() != null) {
            User modifiedBy = userRegistryConnector.getUserByInternalId(userGroupInfo.getModifiedBy().getId());
            userGroupInfo.setModifiedBy(modifiedBy);
        }
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserGroupById userGroupInfo = {}", userGroupInfo);
        log.trace("getUserGroupById end");
        return userGroupInfo;
    }

    @Override
    public Collection<UserGroupInfo> getUserGroups(Optional<String> institutionId, Optional<String> productId, Optional<UUID> userId, Pageable pageable) {
        log.trace("getUserGroups start");
        log.debug("getUserGroups institutionId = {}, productId = {}, userId = {}, pageable = {}", institutionId, productId, userId, pageable);
        Assert.notNull(institutionId, "An optional institutionId is required");
        Assert.notNull(productId, "An optional productId is required");
        Assert.notNull(userId, "An optional userId is required");
        UserGroupFilter userGroupFilter = new UserGroupFilter();
        userGroupFilter.setInstitutionId(institutionId);
        userGroupFilter.setUserId(userId);
        userGroupFilter.setProductId(productId);
        Collection<UserGroupInfo> groupInfos = groupConnector.getUserGroups(userGroupFilter, pageable);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserGroups result = {}", groupInfos);
        log.trace("getUserGroups end");

        return groupInfos;
    }

    @Override
    @Async
    public void deleteMembers(String memberId, String institutionId, String productId) {
        log.trace("deleteMembers start");
        log.debug("deleteMembers memberId = {}, institutionId = {}, productId = {}", memberId, institutionId, productId);
        Assert.hasText(memberId, "Required memberId");
        Assert.hasText(institutionId, "Required institutionId");
        Assert.hasText(productId, "Required productId");
        groupConnector.deleteMembers(memberId, institutionId, productId);
        log.trace("deleteMembers end");
    }

}