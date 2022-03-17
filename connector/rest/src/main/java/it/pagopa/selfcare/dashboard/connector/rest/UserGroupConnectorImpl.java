package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserGroupRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.CreateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UpdateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UserGroupResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.function.Function;

@Slf4j
@Service
public class UserGroupConnectorImpl implements UserGroupConnector {

    private final UserGroupRestClient restClient;

    final static String REQUIRED_GROUP_ID_MESSAGE = "A user group id is required";

    private static final Function<UserGroupResponse, UserGroupInfo> GROUP_RESPONSE_TO_GROUP_INFO = groupResponse -> {
        UserGroupInfo groupInfo = new UserGroupInfo();
        groupInfo.setId(groupResponse.getId());
        groupInfo.setInstitutionId(groupResponse.getInstitutionId());
        groupInfo.setProductId(groupResponse.getProductId());
        groupInfo.setName(groupResponse.getName());
        groupInfo.setDescription(groupResponse.getDescription());
        groupInfo.setStatus(groupResponse.getStatus());
        groupInfo.setMembers(groupResponse.getMembers());
        groupInfo.setCreatedAt(groupResponse.getCreatedAt());
        groupInfo.setCreatedBy(groupResponse.getCreatedBy());
        groupInfo.setModifiedAt(groupResponse.getModifiedAt());
        groupInfo.setModifiedBy(groupResponse.getModifiedBy());
        return groupInfo;
    };

    @Autowired
    public UserGroupConnectorImpl(UserGroupRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void createUserGroup(CreateUserGroup userGroup) {
        log.trace("createUserGroup start");
        log.debug("createUserGroup userGroup = {}", userGroup);
        Assert.notNull(userGroup, "A User Group is required");
        CreateUserGroupRequestDto userGroupRequest = new CreateUserGroupRequestDto();
        userGroupRequest.setDescription(userGroupRequest.getDescription());
        userGroupRequest.setMembers(userGroup.getMembers());
        userGroupRequest.setInstitutionId(userGroup.getInstitutionId());
        userGroupRequest.setProductId(userGroup.getProductId());
        userGroupRequest.setName(userGroupRequest.getName());
        restClient.createUserGroup(userGroupRequest);
        log.trace("createUserGroup end");
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
        userGroupRequest.setDescription(userGroupRequest.getDescription());
        userGroupRequest.setMembers(userGroup.getMembers());
        userGroupRequest.setName(userGroupRequest.getName());
        restClient.updateUserGroupById(id, userGroupRequest);
        log.trace("updateUserGroup end");
    }

    @Override
    public UserGroupInfo getUserGroupById(String id) {
        log.trace("getUserGroupById start");
        log.debug("getUseGroupById id = {}", id);
        UserGroupResponse response = restClient.getUserGroupById(id);
        UserGroupInfo groupInfo = GROUP_RESPONSE_TO_GROUP_INFO.apply(response);
        log.debug("getUseGroupById groupInfo = {}", groupInfo);
        log.trace("getUserGroupById end");
        return groupInfo;
    }
}
