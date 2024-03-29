package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.product.BackOfficeConfigurations;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.web.model.product.BackOfficeConfigurationsResource;
import it.pagopa.selfcare.dashboard.web.model.product.ProductRoleMappingsResource;
import it.pagopa.selfcare.dashboard.web.model.product.ProductsResource;
import it.pagopa.selfcare.dashboard.web.model.product.SubProductResource;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductsMapper {

    public static ProductsResource toResource(ProductTree model) {
        ProductsResource resource = null;
        if (model != null) {
            resource = new ProductsResource();
            resource.setId(model.getNode().getId());
            resource.setLogo(model.getNode().getLogo());
            resource.setLogoBgColor(model.getNode().getLogoBgColor());
            resource.setImageUrl(model.getNode().getDepictImageUrl());
            resource.setTitle(model.getNode().getTitle());
            resource.setDescription(model.getNode().getDescription());
            resource.setUrlPublic(model.getNode().getUrlPublic());
            resource.setUrlBO(model.getNode().getUrlBO());
            resource.setActivatedAt(model.getNode().getActivatedAt());
            resource.setAuthorized(model.getNode().isAuthorized());
            resource.setUserRole(model.getNode().getUserRole());
            resource.setProductOnBoardingStatus(model.getNode().getOnBoardingStatus());
            resource.setStatus(model.getNode().getStatus());
            resource.setDelegable(model.getNode().isDelegable());
            resource.setInvoiceable(model.getNode().isInvoiceable());
            resource.setBackOfficeEnvironmentConfigurations(toProductBackOfficeConfigurations(model.getNode().getBackOfficeEnvironmentConfigurations()));
            if (model.getChildren() != null) {
                resource.setChildren(model.getChildren().stream()
                        .map(ProductsMapper::toChildren)
                        .collect(Collectors.toList()));
            }
        }

        return resource;
    }

    private static SubProductResource toChildren(Product model) {
        SubProductResource resource = null;
        if (model != null) {
            resource = new SubProductResource();
            resource.setId(model.getId());
            resource.setProductOnBoardingStatus(model.getOnBoardingStatus());
            resource.setStatus(model.getStatus());
            resource.setDelegable(model.isDelegable());
            resource.setInvoiceable(model.isInvoiceable());
            resource.setTitle(model.getTitle());
            resource.setImageUrl(model.getDepictImageUrl());
            resource.setLogo(model.getLogo());
            resource.setLogoBgColor(model.getLogoBgColor());
            resource.setDescription(model.getDescription());
            resource.setUrlPublic(model.getUrlPublic());
        }
        return resource;
    }


    public static Collection<BackOfficeConfigurationsResource> toProductBackOfficeConfigurations(Map<String, BackOfficeConfigurations> backOfficeEnvironmentConfigurations) {
        Collection<BackOfficeConfigurationsResource> resource = null;
        if (backOfficeEnvironmentConfigurations != null) {
            resource = backOfficeEnvironmentConfigurations.entrySet().stream()
                    .map(ProductsMapper::toProductBackOfficeConfigurations)
                    .collect(Collectors.toList());
        }
        return resource;
    }


    static BackOfficeConfigurationsResource toProductBackOfficeConfigurations(Map.Entry<String, BackOfficeConfigurations> entry) {
        BackOfficeConfigurationsResource resource = null;
        if (entry != null) {
            resource = new BackOfficeConfigurationsResource();
            resource.setEnvironment(entry.getKey());
            resource.setUrl(entry.getValue().getUrl());
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
