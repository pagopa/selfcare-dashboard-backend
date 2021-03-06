package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.CertifiedField;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.web.model.user_groups.*;

import javax.validation.ValidationException;
import java.util.Optional;
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
        }
        return model;
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
        }
        return model;
    }

    public static UserGroupResource toResource(UserGroupInfo model) {
        UserGroupResource resource = null;
        if (model != null) {
            resource = new UserGroupResource();
            resource.setMembers(model.getMembers().stream()
                    .map(UserMapper::toProductUser)
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
        }
        return resource;
    }

    public static UserGroupIdResource toIdResource(String groupId) {
        UserGroupIdResource resource = null;
        if (groupId != null) {
            resource = new UserGroupIdResource();
            resource.setId(groupId);
        }
        return resource;
    }

    public static PlainUserResource toPlainResource(User model) {
        return Optional.ofNullable(model)
                .map(user -> {
                    PlainUserResource resource = new PlainUserResource();
                    resource.setId(UUID.fromString(user.getId()));
                    Optional.ofNullable(user.getName())
                            .map(CertifiedField::getValue)
                            .ifPresent(resource::setName);
                    Optional.ofNullable(user.getFamilyName())
                            .map(CertifiedField::getValue)
                            .ifPresent(resource::setSurname);
                    return resource;
                }).orElse(null);
    }

    public static UserGroupPlainResource toPlainGroupResource(UserGroupInfo model) {
        UserGroupPlainResource resource = null;
        if (model != null) {
            resource = new UserGroupPlainResource();
            resource.setId(model.getId());
            resource.setName(model.getName());
            resource.setDescription(model.getDescription());
            resource.setInstitutionId(model.getInstitutionId());
            resource.setProductId(model.getProductId());
            resource.setStatus(model.getStatus());
            resource.setCreatedBy(UUID.fromString(model.getCreatedBy().getId()));
            resource.setCreatedAt(model.getCreatedAt());
            if (model.getModifiedBy() != null) {
                resource.setModifiedBy(UUID.fromString(model.getModifiedBy().getId()));
                resource.setModifiedAt(model.getModifiedAt());
            }
            resource.setMembersCount(model.getMembers().size());
        }
        return resource;
    }

}
