package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.web.model.user_groups.CreateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UpdateUserGroupDto;

import javax.validation.ValidationException;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupMapper {
        public static CreateUserGroup fromDto(CreateUserGroupDto dto){
            CreateUserGroup model = null;
            if (dto != null) {
                model = new CreateUserGroup();
                model.setName(dto.getName());
                model.setDescription(dto.getDescription());
                model.setInstitutionId(dto.getInstitutionId());
                model.setProductId(dto.getProductId());
                if (dto.getMembers() == null) {
                    throw new ValidationException("Members list must not be null");
                }
                model.setMembers(dto.getMembers().stream().map(UUID::toString).collect(Collectors.toList()));
                return model;
            }
            return null;
        }

    public static UpdateUserGroup fromDto(UpdateUserGroupDto dto) {
        UpdateUserGroup model = null;
        if (dto != null) {
            model = new UpdateUserGroup();
            model.setDescription(dto.getDescription());
            model.setName(dto.getName());
            if (dto.getMembers() == null) {
                throw new ValidationException("Members list must not be null");
            }
            model.setMembers(dto.getMembers().stream().map(UUID::toString).collect(Collectors.toList()));
            return model;
        }
        return null;
    }
}
