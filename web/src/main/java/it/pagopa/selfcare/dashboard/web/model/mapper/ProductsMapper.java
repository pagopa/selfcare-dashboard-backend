package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.web.model.ProductsResource;

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

}
