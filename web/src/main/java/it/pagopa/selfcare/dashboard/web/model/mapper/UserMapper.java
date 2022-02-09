package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserResource;
import it.pagopa.selfcare.dashboard.web.model.ProductUserResource;
import it.pagopa.selfcare.dashboard.web.model.UserResource;

import java.util.stream.Collectors;

public class UserMapper {

    private static InstitutionUserResource.ProductInfo toUserProductInfo(ProductInfo model) {
        InstitutionUserResource.ProductInfo resource = null;
        if (model != null) {
            resource = new InstitutionUserResource.ProductInfo();
            resource.setId(model.getId());
            resource.setTitle(model.getTitle());
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
            if (model.getProducts() != null) {
                resource.setProducts(model.getProducts().stream()
                        .map(UserMapper::toUserProductInfo)
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
            resource.setRelationshipId(model.getRelationshipId());
            resource.setName(model.getName());
            resource.setSurname(model.getSurname());
            resource.setEmail(model.getEmail());
            resource.setRole(model.getRole());
            resource.setStatus(model.getStatus());
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
