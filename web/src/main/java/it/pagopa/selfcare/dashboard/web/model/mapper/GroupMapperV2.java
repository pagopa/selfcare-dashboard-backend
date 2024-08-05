package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroup;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UserGroupPlainResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMapperV2 {

    @Mapping(target = "membersCount", expression = "java(model.getMembers().size())")
    UserGroupPlainResource toPlainUserGroupResource(UserGroup model);

}
