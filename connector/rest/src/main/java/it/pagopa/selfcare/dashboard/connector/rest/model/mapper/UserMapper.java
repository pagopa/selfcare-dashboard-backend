package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.user.CertifiedField;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.WorkContact;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.CertifiableFieldResourceOfstring;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.CertificationEnum;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserDetailResponse;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.WorkContactResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "email", expression = "java(toCertifiableFieldResourceOfString(userDto.getEmail()))")
    @Mapping(target = "name", expression = "java(toCertifiableFieldResourceOfString(userDto.getName()))")
    @Mapping(target = "familyName", expression = "java(toCertifiableFieldResourceOfString(userDto.getFamilyName()))")
    @Mapping(target = "workContacts", expression = "java(toWorkContacts(userDto.getWorkContacts()))")
    it.pagopa.selfcare.user.generated.openapi.v1.dto.MutableUserFieldsDto toMutableUserFieldsDto(MutableUserFieldsDto userDto);

    @Named("toCertifiableFieldResourceOfString")
    default CertifiableFieldResourceOfstring toCertifiableFieldResourceOfString(CertifiedField<String> field){
        if(field != null && field.getCertification() != null) {
            return CertifiableFieldResourceOfstring.builder()
                    .certification(CertificationEnum.valueOf(field.getCertification().name()))
                    .value(field.getValue())
                    .build();
        }
        return null;
    }

    @Named("toWorkContacts")
    default Map<String, WorkContactResource> toWorkContacts(Map<String, WorkContact> workContactMap){
        Map<String, WorkContactResource> resourceMap = new HashMap<>();
        if(workContactMap != null && !workContactMap.isEmpty()) {
            workContactMap.forEach((s, workContact) -> resourceMap.put(s, WorkContactResource.builder().email(toCertifiableFieldResourceOfString(workContact.getEmail())).build()));
            return resourceMap;
        }
        return null;
    }

    User toUser(UserDetailResponse response);

}
