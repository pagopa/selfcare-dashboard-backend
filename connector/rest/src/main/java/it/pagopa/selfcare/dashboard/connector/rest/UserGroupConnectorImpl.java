package it.pagopa.selfcare.dashboard.connector.rest;

import io.github.resilience4j.retry.annotation.Retry;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.*;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserGroupRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.GroupMapper;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.CreateUserGroupDto;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.PageOfUserGroupResource;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.UpdateUserGroupDto;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserGroupConnectorImpl implements UserGroupConnector {

    private final UserGroupRestClient restClient;
    private final GroupMapper groupMapper;

    static final String REQUIRED_GROUP_ID_MESSAGE = "A user group id is required";

    static final Function<UserGroupResource, UserGroupInfo> GROUP_RESPONSE_TO_GROUP_INFO = groupResponse -> {
        UserGroupInfo groupInfo = new UserGroupInfo();
        groupInfo.setId(groupResponse.getId());
        groupInfo.setInstitutionId(groupResponse.getInstitutionId());
        groupInfo.setProductId(groupResponse.getProductId());
        groupInfo.setName(groupResponse.getName());
        groupInfo.setDescription(groupResponse.getDescription());
        if(Objects.nonNull(groupResponse.getStatus())) {
            groupInfo.setStatus(UserGroupStatus.valueOf(groupResponse.getStatus().getValue()));
        }
        if (groupResponse.getMembers() != null) {
            List<UserInfo> members = groupResponse.getMembers().stream().map(id -> {
                UserInfo member = new UserInfo();
                member.setId(id.toString());
                return member;
            }).toList();
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

    @Override
    public String createUserGroup(CreateUserGroup userGroup) {
        log.trace("createUserGroup start");
        log.debug("createUserGroup userGroup = {}", userGroup);
        Assert.notNull(userGroup, "A User Group is required");
        CreateUserGroupDto userGroupDto = groupMapper.toCreateUserGroupDto(userGroup);
        ResponseEntity<UserGroupResource> responseEntity = restClient._createGroupUsingPOST(userGroupDto);
        UserGroupResource userGroupResource = responseEntity.getBody();
        String groupId = userGroupResource != null ? userGroupResource.getId() : null;
        log.debug("createUserGroup result = {}", groupId);
        log.trace("createUserGroup end");
        return groupId;
    }

    @Override
    public void delete(String groupId) {
        log.trace("delete start");
        log.debug("delete groupId = {}", groupId);
        Assert.hasText(groupId, REQUIRED_GROUP_ID_MESSAGE);
        restClient._deleteGroupUsingDELETE(groupId);
        log.trace("delete end");
    }

    @Override
    public void deleteMembers(String memberId, String institutionId, String productId) {
        log.trace("delete start");
        log.debug("delete memberId = {}, institutionId = {}, productId = {}", memberId, institutionId, productId);
        Assert.hasText(memberId, "Required memberId");
        Assert.hasText(institutionId, "Required institutionId");
        Assert.hasText(productId, "Required productId");
        restClient._deleteMemberFromUserGroupsUsingDELETE(UUID.fromString(memberId), institutionId, productId);
        log.trace("delete end");
    }

    @Override
    public void activate(String groupId) {
        log.trace("activate start");
        log.debug("activate groupId = {}", groupId);
        Assert.hasText(groupId, REQUIRED_GROUP_ID_MESSAGE);
        restClient._activateGroupUsingPOST(groupId);
        log.trace("activate end");
    }

    @Override
    public void suspend(String groupId) {
        log.trace("suspend start");
        log.debug("suspend groupId = {}", groupId);
        Assert.hasText(groupId, REQUIRED_GROUP_ID_MESSAGE);
        restClient._suspendGroupUsingPOST(groupId);
        log.trace("suspend end");
    }

    @Override
    public void updateUserGroup(String id, UpdateUserGroup userGroup) {
        log.trace("updateUserGroup start");
        log.debug("updateUserGroup userGroup = {}", userGroup);
        Assert.hasText(id, REQUIRED_GROUP_ID_MESSAGE);
        Assert.notNull(userGroup, "A User Group is required");
        UpdateUserGroupDto updateUserGroupDto = groupMapper.toUpdateUserGroupDto(userGroup);
        restClient._updateUserGroupUsingPUT(id, updateUserGroupDto);
        log.trace("updateUserGroup end");
    }

    @Override
    @Retry(name = "retryTimeout")
    public UserGroupInfo getUserGroupById(String id) {
        log.trace("getUserGroupById start");
        log.debug("getUseGroupById id = {}", id);
        Assert.hasText(id, REQUIRED_GROUP_ID_MESSAGE);
        UserGroupResource response = restClient._getUserGroupUsingGET(id).getBody();
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
        restClient._addMemberToUserGroupUsingPUT(id, userId);
        log.trace("addMemberToUserGroup end");
    }

    @Override
    public void deleteMemberFromUserGroup(String id, UUID userId) {
        log.trace("deleteMemberFromUserGroup start");
        log.debug("deleteMemberFromUserGroup id = {}, userId = {}", id, userId);
        Assert.hasText(id, REQUIRED_GROUP_ID_MESSAGE);
        Assert.notNull(userId, "A userId is required");
        restClient._deleteMemberFromUserGroupUsingDELETE(id, userId);
        log.trace("deleteMemberFromUserGroup end");
    }

    @Override
    @Retry(name = "retryTimeout")
    public Page<UserGroupInfo> getUserGroups(UserGroupFilter filter, Pageable pageable) {
        log.trace("getUserGroups start");
        log.debug("getUserGroups institutionId = {}, productId = {}, userId = {}, pageable = {}", filter.getInstitutionId(), filter.getProductId(), filter.getUserId(), pageable);

        List<String> sortParams = new ArrayList<>();
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> sortParams.add(order.getProperty() + "," + order.getDirection()));
        }

        final PageOfUserGroupResource userGroupResources = restClient._getUserGroupsUsingGET(filter.getInstitutionId().orElse(null),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortParams,
                filter.getProductId().orElse(null),
                filter.getUserId().orElse(null),
                String.join(",", UserGroupStatus.ACTIVE.name(), UserGroupStatus.SUSPENDED.name())).getBody();

        assert userGroupResources != null;
        final Page<UserGroupInfo> userGroups = convertToUserGroupInfoPage(userGroupResources, pageable);
        log.debug("getUserGroups result = {}", userGroups);
        log.trace("getUserGroups end");
        return userGroups;
    }

    private Page<UserGroupInfo> convertToUserGroupInfoPage(PageOfUserGroupResource userGroupResources, Pageable pageable) {
        return new PageImpl<>(
                userGroupResources.getContent().stream().map(GROUP_RESPONSE_TO_GROUP_INFO).toList(),
                pageable,
                userGroupResources.getTotalElements()
        );
    }
}
