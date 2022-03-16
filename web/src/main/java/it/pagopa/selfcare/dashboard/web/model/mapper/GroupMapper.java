package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.web.model.user_groups.CreateUserGroupDto;

import javax.validation.ValidationException;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupMapper {
        public static CreateUserGroup fromDto(CreateUserGroupDto dto){
            CreateUserGroup group = null;
            if(dto != null){
                group = new CreateUserGroup();
                group.setName(dto.getName());
                group.setDescription(dto.getDescription());
                group.setInstitutionId(dto.getInstitutionId());
                group.setProductId(dto.getProductId());
                if(dto.getMembers() == null){
                    throw new ValidationException("Members list must not be null");
                }
                group.setMembers(dto.getMembers().stream().map(UUID::toString).collect(Collectors.toList()));
                return group;
            }
            return null;
        }
}
