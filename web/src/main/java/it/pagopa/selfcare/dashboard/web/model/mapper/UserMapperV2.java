package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.web.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.web.model.user.UserResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface UserMapperV2 {

    UserResource toUserResource(User model);

    @Mapping(source = "userDto.name", target = "name", qualifiedByName = "mapCertifiedField")
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "workContacts", expression = "java(getEmail(userDto, institutionId))")
    @Mapping(source = "userDto.surname", target = "familyName", qualifiedByName = "mapCertifiedField")
    MutableUserFieldsDto fromUpdateUser(String institutionId, UpdateUserDto userDto);

    @Named("mapCertifiedField")
    default <T> CertifiedField<T> mapCertifiedField(T certifiedField) {
        CertifiedField<T> resource = null;
        if (certifiedField != null) {
            resource = new CertifiedField<>();
            resource.setValue(certifiedField);
            resource.setCertification(Certification.NONE);
        }
        return resource;
    }

    @Named("getEmail")
    default Map<String, WorkContact> getEmail(UpdateUserDto userDto, String institutionId) {
        if (Objects.nonNull(userDto) && Objects.nonNull(institutionId)) {
            WorkContact contact = new WorkContact();
            contact.setEmail(this.mapCertifiedField(userDto.getEmail()));
            return Map.of(institutionId, contact);
        }
        return Map.of();
    }
}
