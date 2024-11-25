package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "familyName", source = "surname")
    it.pagopa.selfcare.user.generated.openapi.v1.dto.UpdateUserRequest toUpdateUserRequest(UpdateUserRequestDto userDto);

    @Named("toCertifiedField")
    default CertifiedField<String> toCertifiedField(CertifiableFieldResponseString field){
        if(Objects.nonNull(field) && Objects.nonNull(field.getCertified())){
            return new CertifiedField<>(Certification.valueOf(field.getCertified().name()), field.getValue());
        }
        return null;
    }

    @Mapping(target = "name",  expression = "java(toCertifiedField(response.getName()))")
    @Mapping(target = "familyName",  expression = "java(toCertifiedField(response.getFamilyName()))")
    @Mapping(target = "email",  expression = "java(toCertifiedField(response.getEmail()))")
    @Mapping(target = "workContacts", expression = "java(toCertifiedWorkContact(response.getWorkContacts()))")
    User toUser(UserDetailResponse response);

    @Mapping(target = "id", source = "userId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "institutionId", source = "institutionId")
    @Mapping(target = "user.id", source = "userResponse.id")
    @Mapping(target = "user.fiscalCode", source = "userResponse.taxCode")
    @Mapping(target = "user.name", expression = "java(toCertifiedField(userResponse.getName()))")
    @Mapping(target = "user.familyName", expression = "java(toCertifiedField(userResponse.getSurname()))")
    @Mapping(target = "user.email", expression = "java(toCertifiedField(userResponse.getEmail()))")
    @Mapping(target = "user.workContacts", expression = "java(toUserInfoContacts(userResponse.getWorkContacts()))")
    @Mapping(target = "role", expression = "java(it.pagopa.selfcare.commons.base.security.PartyRole.valueOf(userDashboardResponse.getRole()).getSelfCareAuthority())")
    @Mapping(target = "products", expression = "java(toProductInfoMap(userDashboardResponse.getProducts()))")
    UserInfo toUserInfo(UserDataResponse userDashboardResponse);


    UserInstitutionWithActionsDto toUserInstitutionWithActionsDto(UserInstitutionWithActions userInstitutionWithActions);

    @Named("toUserInfoContacts")
    default Map<String, WorkContact> toUserInfoContacts(Map<String, String> workContactMap) {
        Map<String, WorkContact> resourceMap = new HashMap<>();
        if (workContactMap != null && !workContactMap.isEmpty()) {
            workContactMap.forEach((key, value) -> {
                WorkContact workContact = new WorkContact();
                workContact.setEmail(toCertifiedField(value));
                resourceMap.put(key, workContact);
            });
        }
        return resourceMap;
    }

    @Named("toCertifiedField")
    default CertifiedField<String> toCertifiedField(String field) {
        CertifiedField<String> certifiedField = new CertifiedField<>();
        certifiedField.setCertification(Certification.NONE);
        certifiedField.setValue(field);
        return certifiedField;
    }

    @Named("toCertifiedWorkContact")
    default Map<String, WorkContact> toCertifiedWorkContact(Map<String, WorkContactResponse> workContacts){
        Map<String, WorkContact> resourceMap = new HashMap<>();
        if(workContacts != null && !workContacts.isEmpty()){
            workContacts.forEach((key, value) -> {
                WorkContact workContact = new WorkContact();
                workContact.setEmail(toCertifiedField(value.getEmail()));
                resourceMap.put(key, workContact);
            });
        }
        return resourceMap;
    }

    @Named("toProductInfoMap")
    default Map<String, ProductInfo> toProductInfoMap(List<OnboardedProductResponse> products) {
        Map<String, ProductInfo> productInfoMap = new HashMap<>();
        if (products != null && !products.isEmpty()) {

            Map<String, List<OnboardedProductResponse>> map = products.stream().collect(Collectors.groupingBy(OnboardedProductResponse::getProductId));
            map.forEach((s, onboardedProducts) -> {
                ProductInfo productInfo = new ProductInfo();
                onboardedProducts.forEach(onboardedProduct -> {
                    RoleInfo roleInfo = new RoleInfo();
                    productInfo.setId(onboardedProduct.getProductId());
                    productInfo.setCreatedAt(onboardedProduct.getCreatedAt());
                    roleInfo.setRole(onboardedProduct.getProductRole());
                    roleInfo.setStatus(onboardedProduct.getStatus().name());
                    roleInfo.setSelcRole(it.pagopa.selfcare.commons.base.security.PartyRole.valueOf(onboardedProduct.getRole()).getSelfCareAuthority());
                    if (productInfo.getRoleInfos() == null)
                        productInfo.setRoleInfos(new ArrayList<>());
                    productInfo.getRoleInfos().add(roleInfo);
                });
                productInfoMap.put(s, productInfo);
            });

        }
        return productInfoMap;
    }

    @Mapping(target = "products", expression = "java(toOnboardedProducts(userInstitutionResponse.getProducts()))")
    UserInstitution toUserInstitution(UserInstitutionResponse userInstitutionResponse);

    @Named("toOnboardedProducts")
    List<OnboardedProduct> toOnboardedProducts(List<OnboardedProductResponse> onboardedProductResponse);

    @Mapping(target = "status", expression = "java(it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.valueOf(onboardedProductResponse.getStatus().name()))")
    @Mapping(target = "env", expression = "java(it.pagopa.selfcare.commons.base.utils.Env.valueOf(onboardedProductResponse.getEnv().name()))")
    @Mapping(target = "role", expression = "java(it.pagopa.selfcare.commons.base.security.PartyRole.valueOf(onboardedProductResponse.getRole()))")
    OnboardedProduct toOnboardedProducts(OnboardedProductResponse onboardedProductResponse);

}
