package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.web.model.*;

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
            resource.setCertification(model.isCertification());
            resource.setName(model.getName());
            resource.setEmail(model.getEmail());
            resource.setSurname(model.getSurname());
            resource.setFiscalCode(model.getFiscalCode());
        }
        return resource;
    }

    public static InstitutionUserResource toInstitutionUser(UserInfo model) {
        InstitutionUserResource resource = null;
        if (model != null) {
            resource = new InstitutionUserResource();
            resource.setId(model.getId());
            resource.setName(model.getName());
            resource.setSurname(model.getSurname());
            resource.setEmail(model.getEmail());
            resource.setRole(model.getRole());
            resource.setStatus(model.getStatus());
            resource.setFiscalCode(model.getTaxCode());
            resource.setCertification(model.isCertified());
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
            resource.setFiscalCode(model.getTaxCode());
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
            model.setProductRole(dto.getProductRole());
        }

        return model;
    }

}
