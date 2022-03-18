package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.web.model.user_groups.CreateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.PlainUserResource;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UpdateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UserGroupResource;

import javax.validation.ValidationException;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupMapper {
    public static CreateUserGroup fromDto(CreateUserGroupDto dto) {
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

    public static UserGroupResource toResource(UserGroupInfo model) {
        UserGroupResource resource = null;
        if (model != null) {
            resource = new UserGroupResource();
            resource.setMembers(model.getMembers().stream()
                    .map(UserMapper::toProductUser)
                    .peek(productUserResource -> productUserResource.setFiscalCode(null))
                    .collect(Collectors.toList()));
            resource.setId(model.getId());
            resource.setName(model.getName());
            resource.setStatus(model.getStatus());
            resource.setDescription(model.getDescription());
            resource.setCreatedAt(model.getCreatedAt());
            resource.setCreatedBy(toPlainResource(model.getCreatedBy()));
            resource.setModifiedAt(model.getModifiedAt());
            resource.setModifiedBy(toPlainResource(model.getModifiedBy()));
            resource.setInstitutionId(model.getInstitutionId());
            resource.setProductId(model.getProductId());
            return resource;
        }
        return null;
    }

    public static PlainUserResource toPlainResource(User model) {
        PlainUserResource resource = null;
        if (model != null) {
            resource = new PlainUserResource();
            resource.setId(model.getId());
            resource.setName(model.getName());
            resource.setSurname(model.getSurname());
            return resource;
        }
        return null;
    }
}
