package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.exception.InternalServerErrorException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidMemberListException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidUserGroupException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupConnector groupConnector;
    private final UserRegistryConnector userRegistryConnector;
    private final InstitutionService institutionService;
    final static String REQUIRED_GROUP_ID_MESSAGE = "A user group id is required";


    @Autowired
    public UserGroupServiceImpl(UserGroupConnector groupConnector, UserRegistryConnector userRegistryConnector, InstitutionService institutionService) {
        this.groupConnector = groupConnector;
        this.userRegistryConnector = userRegistryConnector;
        this.institutionService = institutionService;
    }

    @Override
    public void createUserGroup(CreateUserGroup group) {
        log.trace("createUserGroup start");
        log.debug("createUserGroup group = {}", group);
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(Optional.of(group.getProductId()));

        List<String> retrievedId = retrievedIds(group.getInstitutionId(), group.getProductId(), userInfoFilter);

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

    private List<String> retrievedIds(String groupInstitution, String groupProduct, UserInfo.UserInfoFilter userInfoFilter) {
        Collection<UserInfo> retrievedUsers = institutionService.getInstitutionProductUsers(
                groupInstitution,
                groupProduct,
                userInfoFilter.getRole(),
                userInfoFilter.getProductRoles());
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

        List<String> retrievedId = retrievedIds(userGroupInfo.getInstitutionId(), userGroupInfo.getProductId(), userInfoFilter);

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
        List<UserInfo> userInfos = institutionService.getInstitutionProductUsers(
                userGroupInfo.getInstitutionId(),
                userGroupInfo.getProductId(),
                null,
                null).stream()
                .sorted(userInfoComparator)
                .collect(Collectors.toList());
        userGroupInfo.setMembers(userGroupInfo.getMembers().stream()
                .map(userInfo -> {
                    int index = Collections.binarySearch(userInfos, userInfo, userInfoComparator);
                    if (index < 0) {
                        throw new InternalServerErrorException();
                    }
                    return userInfos.get(index);
                }).collect(Collectors.toList()));
        User createdBy = userRegistryConnector.getUserByInternalId(userGroupInfo.getCreatedBy().getId());
        userGroupInfo.setCreatedBy(createdBy);
        User modifiedBy = userRegistryConnector.getUserByInternalId(userGroupInfo.getModifiedBy().getId());
        userGroupInfo.setModifiedBy(modifiedBy);

        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserGroupById userGroupInfo = {}", userGroupInfo);
        log.trace("getUserGroupById end");
        return userGroupInfo;
    }
}
