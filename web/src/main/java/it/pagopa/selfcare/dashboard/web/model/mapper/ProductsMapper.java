package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.web.model.ProductsResource;

public class ProductsMapper {
    public static ProductsResource toResource(Product entity) {
        ProductsResource resource = null;
        if (entity != null) {
            resource = new ProductsResource();
            resource.setId(entity.getId());
            resource.setLogo(entity.getLogo());
            resource.setTitle(entity.getTitle());
            resource.setDescription(entity.getDescription());
            resource.setUrlPublic(entity.getUrlPublic());
            resource.setUrlBO(entity.getUrlBO());
            resource.setActivationDateTime(entity.getActivationDateTime());
        }

        return resource;
    }
}
