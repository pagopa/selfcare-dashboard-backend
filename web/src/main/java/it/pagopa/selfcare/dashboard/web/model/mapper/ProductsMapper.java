package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.web.model.ProductsResource;

public class ProductsMapper {

    public static ProductsResource toResource(Product model) {
        ProductsResource resource = null;
        if (model != null) {
            resource = new ProductsResource();
            resource.setId(model.getId());
            resource.setCode(model.getCode());
            resource.setLogo(model.getLogo());
            resource.setTitle(model.getTitle());
            resource.setDescription(model.getDescription());
            resource.setUrlPublic(model.getUrlPublic());
            resource.setUrlBO(model.getUrlBO());
            resource.setCreationDateTime(model.getActivationDateTime());
            resource.setActive(model.isActive());
            resource.setAuthorized(model.isAuthorized());
        }

        return resource;
    }

}
