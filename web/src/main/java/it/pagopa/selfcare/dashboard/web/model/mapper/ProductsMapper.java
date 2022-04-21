package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.web.model.ProductRoleMappingsResource;
import it.pagopa.selfcare.dashboard.web.model.ProductsResource;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductsMapper {

    public static ProductsResource toResource(Product model) {
        ProductsResource resource = null;
        if (model != null) {
            resource = new ProductsResource();
            resource.setId(model.getId());
            resource.setLogo(model.getLogo());
            resource.setTitle(model.getTitle());
            resource.setDescription(model.getDescription());
            resource.setUrlPublic(model.getUrlPublic());
            resource.setUrlBO(model.getUrlBO());
            resource.setActivatedAt(model.getActivatedAt());
            resource.setAuthorized(model.isAuthorized());
            resource.setUserRole(model.getUserRole());
            resource.setStatus(model.getStatus());
        }

        return resource;
    }


    public static Collection<ProductRoleMappingsResource> toProductRoleMappingsResource(Map<PartyRole, ProductRoleInfo> roleMappings) {
        Collection<ProductRoleMappingsResource> resource = null;
        if (roleMappings != null) {
            resource = roleMappings.entrySet().stream()
                    .map(ProductsMapper::toProductRoleMappingsResource)
                    .collect(Collectors.toList());
        }
        return resource;
    }


    static ProductRoleMappingsResource toProductRoleMappingsResource(Map.Entry<PartyRole, ProductRoleInfo> entry) {
        ProductRoleMappingsResource resource = null;
        if (entry != null) {
            resource = new ProductRoleMappingsResource();
            resource.setPartyRole(entry.getKey());
            resource.setSelcRole(entry.getKey().getSelfCareAuthority());
            resource.setMultiroleAllowed(entry.getValue().isMultiroleAllowed());
            if (entry.getValue().getRoles() != null) {
                resource.setProductRoles(entry.getValue().getRoles().stream()
                        .map(ProductsMapper::toProductRoleResource)
                        .collect(Collectors.toList()));
            }
        }
        return resource;
    }


    static ProductRoleMappingsResource.ProductRoleResource toProductRoleResource(ProductRoleInfo.ProductRole productRole) {
        ProductRoleMappingsResource.ProductRoleResource resource = null;
        if (productRole != null) {
            resource = new ProductRoleMappingsResource.ProductRoleResource();
            resource.setCode(productRole.getCode());
            resource.setLabel(productRole.getLabel());
            resource.setDescription(productRole.getDescription());
        }
        return resource;
    }

}
