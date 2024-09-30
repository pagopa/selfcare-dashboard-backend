package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroup;
import it.pagopa.selfcare.dashboard.web.model.user_groups.CreateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UpdateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UserGroupPlainResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMapperV2 {

    @Mapping(target = "membersCount", expression = "java(model.getMembers().size())")
    UserGroupPlainResource toPlainUserGroupResource(UserGroup model);

    CreateUserGroup fromDto(CreateUserGroupDto dto);

    UpdateUserGroup fromDto(UpdateUserGroupDto dto);

}
