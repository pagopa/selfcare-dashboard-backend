package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.*;
import it.pagopa.selfcare.dashboard.web.model.product.ProductInfoResource;
import it.pagopa.selfcare.dashboard.web.model.product.ProductRoleInfoResource;
import it.pagopa.selfcare.dashboard.web.model.product.ProductUserResource;

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

    public static UserResource toUserResource(User model) {
        UserResource resource = null;
        if (model != null) {
            resource = new UserResource();
            resource.setId(UUID.fromString(model.getId()));
            resource.setFiscalCode(model.getFiscalCode());
            resource.setName(map(model.getName()));
            resource.setFamilyName(map(model.getFamilyName()));
            resource.setEmail(map(model.getEmail()));
            resource.setWorkContact(model.getWorkContact());
        }
        return resource;
    }

    public static CertifiableFieldResource<String> map(String certifiableField, Certification certification) {
        CertifiableFieldResource<String> resource = null;
        if (certifiableField != null) {
            resource = new CertifiableFieldResource<>();
            resource.setValue(certifiableField);
            resource.setCertification(Certification.valueOf(certification.toString()));
        }
        return resource;
    }

    private static <T> CertifiableFieldResource<T> map(CertifiableField<T> certifiableField) {
        CertifiableFieldResource<T> certifiableFieldResource = null;
        if (certifiableField != null) {
            certifiableFieldResource = new CertifiableFieldResource<>();
            certifiableFieldResource.setValue(certifiableField.getValue());
            certifiableFieldResource.setCertification(certifiableField.getCertification());
        }
        return certifiableFieldResource;
    }

    public static InstitutionUserResource toInstitutionUser(UserInfo model) {
        return toInstitutionUser(model, InstitutionUserResource::new);
    }

    public static InstitutionUserDetailsResource toInstitutionUserDetails(UserInfo model) {
        InstitutionUserDetailsResource resource = toInstitutionUser(model, InstitutionUserDetailsResource::new);
        if (model != null) {
            resource.setFiscalCode(model.getTaxCode());
            resource.setCertification(model.isCertified());
        }

        return resource;
    }

    private static <T extends InstitutionUserResource> T toInstitutionUser(UserInfo model, Supplier<T> supplier) {
        T resource = null;
        if (model != null) {
            resource = supplier.get();
            resource.setId(model.getId());
            resource.setName(model.getName());
            resource.setSurname(model.getSurname());
            resource.setEmail(model.getEmail());
            resource.setRole(model.getRole());
            resource.setStatus(model.getStatus());
            if (model.getProducts() != null) {
                resource.setProducts(model.getProducts().values()
                        .stream()
                        .map(UserMapper::toUserProductInfoResource)
                        .collect(Collectors.toList()));
            }
        }

        return resource;
    }


    public static ProductUserResource toProductUser(UserInfo model) {
        ProductUserResource resource = null;
        if (model != null) {
            resource = new ProductUserResource();
            resource.setId(model.getId());
            resource.setName(model.getName());
            resource.setSurname(model.getSurname());
            resource.setEmail(model.getEmail());
            resource.setRole(model.getRole());
            resource.setStatus(model.getStatus());
            resource.setCertification(model.isCertified());
            if (model.getProducts() != null) {
                resource.setProduct(model.getProducts().values()
                        .stream()
                        .map(UserMapper::toUserProductInfoResource)
                        .collect(Collectors.toList()).get(0));
            }
        }

        return resource;
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

    public static UserDto fromUpdateUser(UpdateUserDto userDto) {
        UserDto model = null;
        if (userDto != null) {
            model = new UserDto();
            model.setEmail(userDto.getEmail());
            model.setName(userDto.getName());
            model.setFamilyName(userDto.getSurname());
//            model.setFiscalCode(userDto.getFiscalCode());
        }
        return model;
    }

}
