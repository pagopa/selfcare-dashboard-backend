package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.exception.InvalidMemberListException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupConnector groupConnector;
    private final InstitutionService institutionService;
    final static String REQUIRED_GROUP_ID_MESSAGE = "A user group id is required";

    @Autowired
    public UserGroupServiceImpl(UserGroupConnector groupConnector, InstitutionService institutionService) {
        this.groupConnector = groupConnector;
        this.institutionService = institutionService;
    }

    @Override
    public void createUserGroup(CreateUserGroup group) {
        log.trace("createUserGroup start");
        log.debug("createUserGroup group = {}", group);
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(Optional.of(group.getProductId()));

        Collection<UserInfo> retrievedUsers = institutionService.getInstitutionProductUsers(group.getInstitutionId(), group.getProductId(), userInfoFilter.getRole(), userInfoFilter.getProductRoles());
        List<String> retrievedId = retrievedUsers.stream()
                .map(UserInfo::getId)
                .collect(Collectors.toList());
        List<String> allowedId = retrievedId.stream()
                .filter(group.getMembers()::contains)
                .sorted()
                .collect(Collectors.toList());
        if (group.getMembers().stream()
                .filter(uuid -> Collections.binarySearch(allowedId, uuid) >= 0)
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

        Collection<UserInfo> retrievedUsers = institutionService.getInstitutionProductUsers(userGroupInfo.getInstitutionId(),
                userGroupInfo.getProductId(),
                userInfoFilter.getRole(),
                userInfoFilter.getProductRoles());
        List<String> retrievedId = retrievedUsers.stream()
                .map(UserInfo::getId)
                .collect(Collectors.toList());
        List<String> allowedId = retrievedId.stream()
                .filter(group.getMembers()::contains)
                .sorted()
                .collect(Collectors.toList());
        if (group.getMembers().stream()
                .filter(uuid -> Collections.binarySearch(allowedId, uuid) >= 0)
                .count() != group.getMembers().size()) {
            throw new InvalidMemberListException("Some members in the list aren't allowed for this institution");
        }
        groupConnector.updateUserGroup(groupId, group);
        log.trace("updateUserGroup end");
    }
}
