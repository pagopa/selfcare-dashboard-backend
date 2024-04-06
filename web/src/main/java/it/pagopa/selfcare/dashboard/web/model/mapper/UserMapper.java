package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserDetailsResource;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserResource;
import it.pagopa.selfcare.dashboard.web.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.web.model.product.ProductInfoResource;
import it.pagopa.selfcare.dashboard.web.model.product.ProductRoleInfoResource;
import it.pagopa.selfcare.dashboard.web.model.product.ProductUserResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserDto;
import it.pagopa.selfcare.dashboard.web.model.user.UserIdResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserProductRoles;
import it.pagopa.selfcare.dashboard.web.model.user.UserResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
            resource.setName(CertifiedFieldMapper.map(model.getName()));
            resource.setFamilyName(CertifiedFieldMapper.map(model.getFamilyName()));
            Optional.ofNullable(model.getWorkContact(institutionId))
                    .map(WorkContact::getEmail)
                    .map(CertifiedFieldMapper::map)
                    .ifPresent(resource::setEmail);
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
            }
            if (model.getProducts() != null) {
                resource.setProducts(model.getProducts().values().stream()
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
            resource.setId(UUID.fromString(model.getId()));
            resource.setRole(model.getRole());
            resource.setStatus(model.getStatus());
            if (model.getUser() != null) {
                resource.setName(CertifiedFieldMapper.toValue(model.getUser().getName()));
                resource.setSurname(CertifiedFieldMapper.toValue(model.getUser().getFamilyName()));
                Optional.ofNullable(model.getUser().getWorkContacts())
                        .map(map -> map.get(model.getInstitutionId()))
                        .map(WorkContact::getEmail)
                        .map(CertifiedFieldMapper::toValue)
                        .ifPresent(resource::setEmail);
            }
            if (model.getProducts() != null) {
                resource.setProduct(model.getProducts().values().stream()
                        .map(UserMapper::toUserProductInfoResource)
                        .collect(Collectors.toList())
                        .get(0));
            }
        }
        return resource;
    }


    public static it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto fromCreateUserDto(CreateUserDto dto, String institutionId) {
        it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto model = null;
        if (dto != null) {
            model = new it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto();
            model.setName(dto.getName() == null ? "" : dto.getName());//TODO: remove after Party API changes
            model.setSurname(dto.getSurname() == null ? "" : dto.getSurname());//TODO: remove after Party API changes
            model.setTaxCode(dto.getTaxCode() == null ? "" : dto.getTaxCode());//TODO: remove after Party API changes
            model.setEmail(dto.getEmail() == null ? "" : dto.getEmail());//TODO: remove after Party API changes
            if (dto.getProductRoles() != null) {
                model.setRoles(dto.getProductRoles().stream()
                        .map(productRole -> {
                            it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto.Role role = new it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto.Role();
                            role.setProductRole(productRole);
                            return role;
                        }).collect(Collectors.toSet()));
            }
            model.setUser(toSaveUserDto(dto, institutionId));
        }

        return model;
    }

    //TODO create temp mapper that sets fields to "" string
    public static it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto toCreateUserDto(UserProductRoles roles) {
        it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto resource = null;
        if (roles != null) {
            resource = new it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto();
            resource.setName("");
            resource.setSurname("");
            resource.setEmail("");
            resource.setTaxCode("");
            if (roles.getProductRoles() != null)
                resource.setRoles(roles.getProductRoles().stream().map(productRole -> {
                    it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto.Role role = new it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto.Role();
                    role.setProductRole(productRole);
                    return role;
                }).collect(Collectors.toSet()));
        }
        return resource;
    }

    private static SaveUserDto toSaveUserDto(CreateUserDto model, String institutionId) {
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


    public static MutableUserFieldsDto fromUpdateUser(UpdateUserDto userDto, String institutionId) {
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


    public static SaveUserDto map(UserDto model, String institutionId) {
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
