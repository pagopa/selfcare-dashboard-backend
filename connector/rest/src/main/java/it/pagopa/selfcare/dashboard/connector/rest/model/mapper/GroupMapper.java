package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.CreateUserGroupDto;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.UpdateUserGroupDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    CreateUserGroupDto toCreateUserGroupDto(CreateUserGroup createUserGroup);

    UpdateUserGroupDto toUpdateUserGroupDto(UpdateUserGroup updateUserGroup);
}
