package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.HashMap;
import java.util.List;
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

    @Mapping(target = "products", expression = "java(toOnboardedProducts(userInstitutionResponse.getProducts()))")
    UserInstitution toUserInstitution(UserInstitutionResponse userInstitutionResponse);

    @Named("toOnboardedProducts")
    List<OnboardedProduct> toOnboardedProducts(List<OnboardedProductResponse> onboardedProductResponse);

    @Mapping(target = "status", expression = "java(it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.valueOf(onboardedProductResponse.getStatus().name()))")
    @Mapping(target = "env", expression = "java(it.pagopa.selfcare.commons.base.utils.Env.valueOf(onboardedProductResponse.getEnv().name()))")
    @Mapping(target = "role", expression = "java(it.pagopa.selfcare.commons.base.security.PartyRole.valueOf(onboardedProductResponse.getRole().name()))")
    OnboardedProduct toOnboardedProducts(OnboardedProductResponse onboardedProductResponse);

}
