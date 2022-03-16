package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserGroupRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UserGroupRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
public class UserGroupConnectorImpl implements UserGroupConnector {

    private final UserGroupRestClient restClient;

    final static String REQUIRED_GROUP_ID_MESSAGE = "A user group id is required";

    @Autowired
    public UserGroupConnectorImpl(UserGroupRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void createUserGroup(CreateUserGroup userGroup) {
        log.trace("createUserGroup start");
        log.debug("createUserGroup userGroup = {}", userGroup);
        Assert.notNull(userGroup, "A User Group is required");
        UserGroupRequestDto userGroupRequest = new UserGroupRequestDto();
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
}
