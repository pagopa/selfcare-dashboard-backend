package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.CreateUserGroupDto;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.UpdateUserGroupDto;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    CreateUserGroupDto toCreateUserGroupDto(CreateUserGroup createUserGroup);

    UpdateUserGroupDto toUpdateUserGroupDto(UpdateUserGroup updateUserGroup);
    UserGroup toUserGroup(UserGroupResource userGroupResource);

    @Mapping(target = "createdBy", expression = "java(toUser(userGroupResource.getCreatedBy()))")
    @Mapping(target = "modifiedBy", expression = "java(toUser(userGroupResource.getModifiedBy()))")
    @Mapping(target = "members", expression = "java(toUserInfo(userGroupResource.getMembers()))")
    UserGroupInfo toUserGroupInfo(UserGroupResource userGroupResource);

    @Named("toUserInfo")
    default List<UserInfo> toUserInfo(List<UUID> uuidList) {
        if(uuidList == null) {
            return Collections.emptyList();
        }
        return uuidList.stream().map(id -> {
            UserInfo member = new UserInfo();
            member.setId(id.toString());
            return member;
        }).toList();
    }

    @Named("toUser")
    default User toUser(String userId) {
        if(userId == null) {
            return null;
        }
        User user = new User();
        user.setId(userId);
        return user;
    }
}
