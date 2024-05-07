package it.pagopa.selfcare.dashboard.connector.model.product.mapper;

import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.product.entity.Product;

import java.util.List;

public interface ProductMapper {
    List<ProductTree> toTreeResource(List<Product> model);
}
