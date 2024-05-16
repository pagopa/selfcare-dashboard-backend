package it.pagopa.selfcare.dashboard.connector.rest;

import io.github.resilience4j.retry.annotation.Retry;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.*;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserGroupRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.CreateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UpdateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UserGroupResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserGroupConnectorImpl implements UserGroupConnector {

    private final UserGroupRestClient restClient;

    static final String REQUIRED_GROUP_ID_MESSAGE = "A user group id is required";

    static final Function<UserGroupResponse, UserGroupInfo> GROUP_RESPONSE_TO_GROUP_INFO = groupResponse -> {
        UserGroupInfo groupInfo = new UserGroupInfo();
        groupInfo.setId(groupResponse.getId());
        groupInfo.setInstitutionId(groupResponse.getInstitutionId());
        groupInfo.setProductId(groupResponse.getProductId());
        groupInfo.setName(groupResponse.getName());
        groupInfo.setDescription(groupResponse.getDescription());
        groupInfo.setStatus(groupResponse.getStatus());
        if (groupResponse.getMembers() != null) {
            List<UserInfo> members = groupResponse.getMembers().stream().map(id -> {
                UserInfo member = new UserInfo();
                member.setId(id);
                return member;
            }).collect(Collectors.toList());
            groupInfo.setMembers(members);
        }
        groupInfo.setCreatedAt(groupResponse.getCreatedAt());
        groupInfo.setModifiedAt(groupResponse.getModifiedAt());
        User createdBy = new User();
        createdBy.setId(groupResponse.getCreatedBy());
        groupInfo.setCreatedBy(createdBy);

        if (groupResponse.getModifiedBy() != null) {
            User userInfo = new User();
            userInfo.setId(groupResponse.getModifiedBy());
            groupInfo.setModifiedBy(userInfo);
        }

        return groupInfo;
    };

    @Autowired
    public UserGroupConnectorImpl(UserGroupRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String createUserGroup(CreateUserGroup userGroup) {
        log.trace("createUserGroup start");
        log.debug("createUserGroup userGroup = {}", userGroup);
        Assert.notNull(userGroup, "A User Group is required");
        CreateUserGroupRequestDto userGroupRequest = new CreateUserGroupRequestDto();
        userGroupRequest.setDescription(userGroup.getDescription());
        userGroupRequest.setMembers(userGroup.getMembers());
        userGroupRequest.setInstitutionId(userGroup.getInstitutionId());
        userGroupRequest.setProductId(userGroup.getProductId());
        userGroupRequest.setName(userGroup.getName());
        userGroupRequest.setStatus(UserGroupStatus.ACTIVE);
        String groupId = restClient.createUserGroup(userGroupRequest).getId();
        log.debug("createUserGroup result = {}", groupId);
        log.trace("createUserGroup end");
        return groupId;
    }

    @Override
    public void delete(String groupId) {
        log.trace("delete start");
        log.debug("delete groupId = {}", groupId);
        Assert.hasText(groupId, REQUIRED_GROUP_ID_MESSAGE);
        restClient.deleteUserGroupById(groupId);
        log.trace("delete end");
    }

    @Override
    public void deleteMembers(String memberId, String institutionId, String productId) {
        log.trace("delete start");
        log.debug("delete memberId = {}, institutionId = {}, productId = {}", memberId, institutionId, productId);
        Assert.hasText(memberId, "Required memberId");
        Assert.hasText(institutionId, "Required institutionId");
        Assert.hasText(productId, "Required productId");
        restClient.deleteMembers(UUID.fromString(memberId), institutionId, productId);
        log.trace("delete end");
    }

    @Override
    public void activate(String groupId) {
        log.trace("activate start");
        log.debug("activate groupId = {}", groupId);
        Assert.hasText(groupId, REQUIRED_GROUP_ID_MESSAGE);
        restClient.activateUserGroupById(groupId);
        log.trace("activate end");
    }

    @Override
    public void suspend(String groupId) {
        log.trace("suspend start");
        log.debug("suspend groupId = {}", groupId);
        Assert.hasText(groupId, REQUIRED_GROUP_ID_MESSAGE);
        restClient.suspendUserGroupById(groupId);
        log.trace("suspend end");
    }

    @Override
    public void updateUserGroup(String id, UpdateUserGroup userGroup) {
        log.trace("updateUserGroup start");
        log.debug("updateUserGroup userGroup = {}", userGroup);
        Assert.hasText(id, REQUIRED_GROUP_ID_MESSAGE);
        Assert.notNull(userGroup, "A User Group is required");
        UpdateUserGroupRequestDto userGroupRequest = new UpdateUserGroupRequestDto();
        userGroupRequest.setDescription(userGroup.getDescription());
        userGroupRequest.setMembers(userGroup.getMembers());
        userGroupRequest.setName(userGroup.getName());
        restClient.updateUserGroupById(id, userGroupRequest);
        log.trace("updateUserGroup end");
    }

    @Override
    @Retry(name = "retryTimeout")
    public UserGroupInfo getUserGroupById(String id) {
        log.trace("getUserGroupById start");
        log.debug("getUseGroupById id = {}", id);
        Assert.hasText(id, REQUIRED_GROUP_ID_MESSAGE);
        UserGroupResponse response = restClient.getUserGroupById(id);
        UserGroupInfo groupInfo = GROUP_RESPONSE_TO_GROUP_INFO.apply(response);
        log.debug("getUseGroupById groupInfo = {}", groupInfo);
        log.trace("getUserGroupById end");
        return groupInfo;
    }

    @Override
    public void addMemberToUserGroup(String id, UUID userId) {
        log.trace("addMemberToUserGroup start");
        log.debug("addMemberToUserGroup id = {}, userId = {}", id, userId);
        Assert.hasText(id, REQUIRED_GROUP_ID_MESSAGE);
        Assert.notNull(userId, "A userId is required");
        restClient.addMemberToUserGroup(id, userId);
        log.trace("addMemberToUserGroup end");
    }

    @Override
    public void deleteMemberFromUserGroup(String id, UUID userId) {
        log.trace("deleteMemberFromUserGroup start");
        log.debug("deleteMemberFromUserGroup id = {}, userId = {}", id, userId);
        Assert.hasText(id, REQUIRED_GROUP_ID_MESSAGE);
        Assert.notNull(userId, "A userId is required");
        restClient.deleteMemberFromUserGroup(id, userId);
        log.trace("deleteMemberFromUserGroup end");
    }

    @Override
    @Retry(name = "retryTimeout")
    public Page<UserGroupInfo> getUserGroups(UserGroupFilter filter, Pageable pageable) {
        log.trace("getUserGroups start");
        log.debug("getUserGroups institutionId = {}, productId = {}, userId = {}, pageable = {}", filter.getInstitutionId(), filter.getProductId(), filter.getUserId(), pageable);
        final Page<UserGroupInfo> userGroups = restClient.getUserGroups(filter.getInstitutionId().orElse(null),
                        filter.getProductId().orElse(null),
                        filter.getUserId().orElse(null),
                        List.of(UserGroupStatus.ACTIVE, UserGroupStatus.SUSPENDED),
                        pageable)
                .map(GROUP_RESPONSE_TO_GROUP_INFO);
        log.debug("getUserGroups result = {}", userGroups);
        log.trace("getUserGroups end");
        return userGroups;
    }

}
