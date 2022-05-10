package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserDetailsResource;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserResource;
import it.pagopa.selfcare.dashboard.web.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.web.model.product.ProductInfoResource;
import it.pagopa.selfcare.dashboard.web.model.product.ProductRoleInfoResource;
import it.pagopa.selfcare.dashboard.web.model.product.ProductUserResource;
import it.pagopa.selfcare.dashboard.web.model.user.CertifiedFieldResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserDto;
import it.pagopa.selfcare.dashboard.web.model.user.UserIdResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserResource;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UserMapper {

    private static ProductInfoResource toUserProductInfoResource(ProductInfo model) {
        ProductInfoResource resource = null;
        if (model != null) {
            resource = new ProductInfoResource();
            resource.setId(model.getId());
            resource.setTitle(model.getTitle());
            resource.setRoleInfos(model.getRoleInfos()
                    .stream()
                    .map(UserMapper::toRoleInfoResource)
                    .collect(Collectors.toList())
            );
        }
        return resource;
    }


    private static ProductRoleInfoResource toRoleInfoResource(RoleInfo model) {
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


    public static SaveUserDto map(UserDto model, String institutionId) {
        SaveUserDto resource = null;
        if (model != null) {
            resource = new SaveUserDto();
            resource.setName(map(model.getName()));
            resource.setEmail(map(model.getEmail()));
            resource.setFamilyName(map(model.getSurname()));
            resource.setFiscalCode(model.getFiscalCode());
            if (institutionId != null) {
                WorkContact contact = new WorkContact();
                contact.setEmail(map(model.getEmail()));
                resource.setWorkContacts(Map.of(institutionId, contact));
            }
        }
        return resource;
    }


    public static UserIdResource toIdResource(UserId model) {
        UserIdResource resource = null;
        if (model != null) {
            resource = new UserIdResource();
            resource.setId(model.getId());
        }
        return resource;
    }


    public static UserResource toUserResource(User model, String institutionId) {
        UserResource resource = null;
        if (model != null) {
            resource = new UserResource();
            resource.setId(UUID.fromString(model.getId()));
            resource.setFiscalCode(model.getFiscalCode());
            resource.setName(toCertifiedFieldResource(model.getName()));
            resource.setFamilyName(toCertifiedFieldResource(model.getFamilyName()));
            if (institutionId != null) {
                if (model.getWorkContacts() != null)
                    resource.setEmail(model.getWorkContacts().entrySet().stream()
                            .filter(e -> e.getKey().equals(institutionId))
                            .findAny()
                            .map(entry -> toCertifiedFieldResource(entry.getValue().getEmail()))
                            .orElse(null));
            }
        }
        return resource;
    }


    public static CertifiedField<String> map(String certifiableField) {
        CertifiedField<String> resource = null;
        if (certifiableField != null) {
            resource = new CertifiedField<>();
            resource.setValue(certifiableField);
            resource.setCertification(Certification.NONE);
        }
        return resource;
    }


    public static <T> CertifiedFieldResource<T> toCertifiedFieldResource(CertifiedField<T> certifiedField) {
        CertifiedFieldResource<T> resource = null;
        if (certifiedField != null) {
            resource = new CertifiedFieldResource<>();
            resource.setCertified(Certification.isCertified(certifiedField.getCertification()));
            resource.setValue(certifiedField.getValue());
        }
        return resource;
    }


    public static InstitutionUserResource toInstitutionUser(UserInfo model) {
        return toInstitutionUser(model, InstitutionUserResource::new);
    }


    public static InstitutionUserDetailsResource toInstitutionUserDetails(UserInfo model) {
        InstitutionUserDetailsResource resource = toInstitutionUser(model, InstitutionUserDetailsResource::new);
        if (resource != null) {
            Optional.ofNullable(model)
                    .map(UserInfo::getUser)
                    .map(User::getFiscalCode)
                    .ifPresent(resource::setFiscalCode);
        }
        return resource;
    }


    private static <T extends InstitutionUserResource> T toInstitutionUser(UserInfo model, Supplier<T> supplier) {
        return Optional.ofNullable(model)
                .map(userInfo -> {
                    final T resource = supplier.get();
                    resource.setId(UUID.fromString(userInfo.getId()));
                    resource.setRole(userInfo.getRole());
                    resource.setStatus(userInfo.getStatus());
                    Optional.ofNullable(userInfo.getUser()).ifPresent(userResource -> {
                        Optional.ofNullable(userResource.getName())
                                .map(CertifiedField::getValue)
                                .ifPresent(resource::setName);
                        Optional.ofNullable(userResource.getFamilyName())
                                .map(CertifiedField::getValue)
                                .ifPresent(resource::setSurname);
                        Optional.ofNullable(userResource.getEmail())
                                .map(CertifiedField::getValue)
                                .ifPresent(resource::setEmail);
                    });
                    Optional.ofNullable(userInfo.getProducts())
                            .map(map -> map.values().stream()
                                    .map(UserMapper::toUserProductInfoResource)
                                    .collect(Collectors.toList()))
                            .ifPresent(resource::setProducts);
                    return resource;
                }).orElse(null);
    }


    public static ProductUserResource toProductUser(UserInfo model) {
        return Optional.ofNullable(model)
                .map(userInfo -> {
                    ProductUserResource resource = new ProductUserResource();
                    resource.setId(UUID.fromString(userInfo.getId()));
                    resource.setRole(userInfo.getRole());
                    resource.setStatus(userInfo.getStatus());
                    Optional.ofNullable(userInfo.getUser()).ifPresent(userResource -> {
                        resource.setName(Optional.ofNullable(userResource.getName())
                                .map(CertifiedField::getValue)
                                .orElse(null));
                        resource.setSurname(Optional.ofNullable(userResource.getFamilyName())
                                .map(CertifiedField::getValue)
                                .orElse(null));
                        resource.setEmail(Optional.ofNullable(userResource.getEmail())
                                .map(CertifiedField::getValue)
                                .orElse(null));
                    });
                    Optional.ofNullable(userInfo.getProducts()).ifPresent(map ->
                            resource.setProduct(map.values()
                                    .stream()
                                    .map(UserMapper::toUserProductInfoResource)
                                    .collect(Collectors.toList())
                                    .get(0)));
                    return resource;
                })
                .orElse(null);
    }


    public static it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto fromCreateUserDto(CreateUserDto dto) {
        it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto model = null;
        if (dto != null) {
            model = new it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto();
            model.setName(dto.getName());
            model.setSurname(dto.getSurname());
            model.setTaxCode(dto.getTaxCode());
            model.setEmail(dto.getEmail());
            if (dto.getProductRoles() != null) {
                model.setRoles(dto.getProductRoles().stream()
                        .map(productRole -> {
                            it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto.Role role = new it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto.Role();
                            role.setProductRole(productRole);
                            return role;
                        }).collect(Collectors.toSet()));
            }
        }

        return model;
    }


    public static MutableUserFieldsDto fromUpdateUser(UpdateUserDto userDto, String institutionId) {
        MutableUserFieldsDto resource = null;
        if (userDto != null) {
            resource = new MutableUserFieldsDto();
            resource.setName(map(userDto.getName()));
            resource.setEmail(map(userDto.getEmail()));
            resource.setFamilyName(map(userDto.getSurname()));
            if (institutionId != null) {
                WorkContact contact = new WorkContact();
                contact.setEmail(map(userDto.getEmail()));
                resource.setWorkContacts(Map.of(institutionId, contact));
            }
        }
        return resource;
    }

}
