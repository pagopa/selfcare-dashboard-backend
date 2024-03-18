package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        }
        return null;
    }

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
                    roleInfo.setRole(onboardedProduct.getProductRole());
                    roleInfo.setStatus(onboardedProduct.getStatus().name());
                    roleInfo.setSelcRole(it.pagopa.selfcare.commons.base.security.PartyRole.valueOf(onboardedProduct.getRole().name()).getSelfCareAuthority());
                    if (productInfo.getRoleInfos() == null)
                        productInfo.setRoleInfos(new ArrayList<>());
                    productInfo.getRoleInfos().add(roleInfo);
                });
                productInfoMap.put(s, productInfo);
            });

        }
        return productInfoMap;
    }

}
