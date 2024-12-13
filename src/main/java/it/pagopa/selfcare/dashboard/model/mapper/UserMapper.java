package it.pagopa.selfcare.dashboard.model.mapper;

import it.pagopa.selfcare.dashboard.model.user.*;
import it.pagopa.selfcare.dashboard.model.*;
import it.pagopa.selfcare.dashboard.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.model.product.*;
import it.pagopa.selfcare.dashboard.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.model.user.User;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.*;
import java.util.function.Supplier;
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
    @Mapping(target = "mobilePhone",  expression = "java(toCertifiedField(response.getMobilePhone()))")
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
    @Mapping(target = "user.mobilePhone", expression = "java(toCertifiedField(userResponse.getMobilePhone()))")    UserInfo toUserInfo(UserDataResponse userDashboardResponse);
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

    @Mapping(target = "status", expression = "java(it.pagopa.selfcare.dashboard.model.institution.RelationshipState.valueOf(onboardedProductResponse.getStatus().name()))")
    @Mapping(target = "env", expression = "java(it.pagopa.selfcare.commons.base.utils.Env.valueOf(onboardedProductResponse.getEnv().name()))")
    @Mapping(target = "role", expression = "java(it.pagopa.selfcare.commons.base.security.PartyRole.valueOf(onboardedProductResponse.getRole()))")
    OnboardedProduct toOnboardedProducts(OnboardedProductResponse onboardedProductResponse);

    // Methods from the provided class with default implementations
    default ProductInfoResource toUserProductInfoResource(ProductInfo model) {
        ProductInfoResource resource = null;
        if (model != null) {
            resource = new ProductInfoResource();
            resource.setId(model.getId());
            resource.setTitle(model.getTitle());
            resource.setRoleInfos(model.getRoleInfos()
                    .stream()
                    .map(this::toRoleInfoResource)
                    .toList()
            );
        }
        return resource;
    }

    default ProductRoleInfoResource toRoleInfoResource(RoleInfo model) {
        ProductRoleInfoResource resource = null;
        if (model != null) {
            resource = new ProductRoleInfoResource();
            resource.setRelationshipId(model.getRelationshipId());
            resource.setRole(model.getRole());
            resource.setSelcRole(model.getSelcRole());
            resource.setStatus(model.getStatus());
        }
        return resource;
    }

    default UserIdResource toIdResource(UserId model) {
        UserIdResource resource = null;
        if (model != null) {
            resource = new UserIdResource();
            resource.setId(model.getId());
        }
        return resource;
    }

    default UserResource toUserResource(User model, String institutionId) {
        UserResource resource = null;
        if (model != null) {
            resource = new UserResource();
            resource.setId(UUID.fromString(model.getId()));
            resource.setFiscalCode(model.getFiscalCode());
            resource.setName(CertifiedFieldMapper.map(model.getName()));
            resource.setFamilyName(CertifiedFieldMapper.map(model.getFamilyName()));
            Optional.ofNullable(model.getWorkContact(institutionId))
                    .map(WorkContact::getEmail)
                    .map(CertifiedFieldMapper::map)
                    .ifPresent(resource::setEmail);
        }
        return resource;
    }

    default InstitutionUserResource toInstitutionUser(UserInfo model) {
        return toInstitutionUser(model, InstitutionUserResource::new);
    }

    default InstitutionUserDetailsResource toInstitutionUserDetails(UserInfo model) {
        InstitutionUserDetailsResource resource = toInstitutionUser(model, InstitutionUserDetailsResource::new);
        if (resource != null) {
            Optional.ofNullable(model)
                    .map(UserInfo::getUser)
                    .map(User::getFiscalCode)
                    .ifPresent(resource::setFiscalCode);
        }
        return resource;
    }

    default <T extends InstitutionUserResource> T toInstitutionUser(UserInfo model, Supplier<T> supplier) {
        T resource = null;
        if (model != null) {
            resource = supplier.get();
            resource.setId(UUID.fromString(model.getId()));
            resource.setRole(model.getRole());
            resource.setStatus(model.getStatus());
            if (model.getUser() != null) {
                resource.setName(CertifiedFieldMapper.toValue(model.getUser().getName()));
                resource.setSurname(CertifiedFieldMapper.toValue(model.getUser().getFamilyName()));
                resource.setEmail(CertifiedFieldMapper.toValue(model.getUser().getEmail()));
                resource.setMobilePhone(CertifiedFieldMapper.toValue(model.getUser().getMobilePhone()));
            }
            if (model.getProducts() != null) {
                resource.setProducts(model.getProducts().values().stream()
                        .map(this::toUserProductInfoResource)
                        .toList());
            }
        }
        return resource;
    }

    default ProductUserResource toProductUser(UserInfo model) {
        ProductUserResource resource = null;
        if (model != null) {
            resource = new ProductUserResource();
            resource.setId(UUID.fromString(model.getId()));
            resource.setRole(model.getRole());
            resource.setStatus(model.getStatus());
            if (model.getUser() != null) {
                resource.setName(CertifiedFieldMapper.toValue(model.getUser().getName()));
                resource.setSurname(CertifiedFieldMapper.toValue(model.getUser().getFamilyName()));
                Optional.ofNullable(model.getUser().getWorkContacts())
                        .map(map -> map.get(model.getUserMailUuid() != null ? model.getUserMailUuid() : model.getInstitutionId()))
                        .map(WorkContact::getEmail)
                        .map(CertifiedFieldMapper::toValue)
                        .ifPresent(resource::setEmail);
            }
            if (model.getProducts() != null) {
                resource.setProduct(model.getProducts().values().stream()
                        .map(this::toUserProductInfoResource)
                        .toList()
                        .get(0));
            }
        }
        return resource;
    }

    default List<ProductUserResource> toProductUsers(UserInfo model) {
        List<ProductUserResource> response = new ArrayList<>();
        if (model != null && model.getProducts() != null && !model.getProducts().isEmpty()) {
            model.getProducts().forEach((s, productInfo) -> {
                ProductUserResource resource = new ProductUserResource();
                resource.setId(UUID.fromString(model.getId()));
                resource.setRole(model.getRole());
                resource.setStatus(model.getStatus());
                if (model.getUser() != null) {
                    resource.setName(CertifiedFieldMapper.toValue(model.getUser().getName()));
                    resource.setSurname(CertifiedFieldMapper.toValue(model.getUser().getFamilyName()));
                    resource.setFiscalCode(model.getUser().getFiscalCode());
                    Optional.ofNullable(model.getUser().getWorkContacts())
                            .map(map -> map.get(model.getUserMailUuid()))
                            .map(WorkContact::getEmail)
                            .map(CertifiedFieldMapper::toValue)
                            .ifPresent(resource::setEmail);
                }
                resource.setProduct(toUserProductInfoResource(productInfo));
                resource.setCreatedAt(productInfo.getCreatedAt());
                response.add(resource);
            });
        }
        return response;
    }

    default it.pagopa.selfcare.dashboard.model.user.CreateUserDto fromCreateUserDto(CreateUserDto dto, String institutionId) {
        it.pagopa.selfcare.dashboard.model.user.CreateUserDto model = null;
        if (dto != null) {
            model = new it.pagopa.selfcare.dashboard.model.user.CreateUserDto();
            model.setName(dto.getName() == null ? "" : dto.getName());//TODO: remove after Party API changes
            model.setSurname(dto.getSurname() == null ? "" : dto.getSurname());//TODO: remove after Party API changes
            model.setTaxCode(dto.getTaxCode() == null ? "" : dto.getTaxCode());//TODO: remove after Party API changes
            model.setEmail(dto.getEmail() == null ? "" : dto.getEmail());//TODO: remove after Party API changes
            if (dto.getProductRoles() != null) {
                model.setRoles(dto.getProductRoles().stream()
                        .map(productRole -> {
                            it.pagopa.selfcare.dashboard.model.user.CreateUserDto.Role role = new it.pagopa.selfcare.dashboard.model.user.CreateUserDto.Role();
                            role.setProductRole(productRole);
                            return role;
                        }).collect(Collectors.toSet()));
            }
            model.setUser(toSaveUserDto(dto, institutionId));
        }

        return model;
    }

    default it.pagopa.selfcare.dashboard.model.user.CreateUserDto toCreateUserDto(UserProductRoles roles) {
        it.pagopa.selfcare.dashboard.model.user.CreateUserDto resource = null;
        if (roles != null) {
            resource = new it.pagopa.selfcare.dashboard.model.user.CreateUserDto();
            resource.setName("");
            resource.setSurname("");
            resource.setEmail("");
            resource.setTaxCode("");
            if (roles.getProductRoles() != null)
                resource.setRoles(roles.getProductRoles().stream().map(productRole -> {
                    it.pagopa.selfcare.dashboard.model.user.CreateUserDto.Role role = new it.pagopa.selfcare.dashboard.model.user.CreateUserDto.Role();
                    role.setProductRole(productRole);
                    return role;
                }).collect(Collectors.toSet()));
        }
        return resource;
    }

    default SaveUserDto toSaveUserDto(CreateUserDto model, String institutionId) {
        SaveUserDto resource = null;
        if (model != null) {
            resource = new SaveUserDto();
            resource.setFiscalCode(model.getTaxCode());
            resource.setName(CertifiedFieldMapper.map(model.getName()));
            resource.setFamilyName(CertifiedFieldMapper.map(model.getSurname()));
            if (institutionId != null) {
                WorkContact contact = new WorkContact();
                contact.setEmail(CertifiedFieldMapper.map(model.getEmail()));
                resource.setWorkContacts(Map.of(institutionId, contact));
            }
        }
        return resource;
    }

    default MutableUserFieldsDto fromUpdateUser(UpdateUserDto userDto, String institutionId) {
        MutableUserFieldsDto resource = null;
        if (userDto != null) {
            resource = new MutableUserFieldsDto();
            resource.setName(CertifiedFieldMapper.map(userDto.getName()));
            resource.setFamilyName(CertifiedFieldMapper.map(userDto.getSurname()));
            if (institutionId != null) {
                WorkContact contact = new WorkContact();
                contact.setEmail(CertifiedFieldMapper.map(userDto.getEmail()));
                resource.setWorkContacts(Map.of(institutionId, contact));
            }
        }
        return resource;
    }

    default SaveUserDto map(UserDto model, String institutionId) {
        SaveUserDto resource = null;
        if (model != null) {
            resource = new SaveUserDto();
            resource.setName(CertifiedFieldMapper.map(model.getName()));
            resource.setFamilyName(CertifiedFieldMapper.map(model.getSurname()));
            resource.setFiscalCode(model.getFiscalCode());
            if (institutionId != null) {
                WorkContact contact = new WorkContact();
                contact.setEmail(CertifiedFieldMapper.map(model.getEmail()));
                resource.setWorkContacts(Map.of(institutionId, contact));
            }
        }
        return resource;
    }
}