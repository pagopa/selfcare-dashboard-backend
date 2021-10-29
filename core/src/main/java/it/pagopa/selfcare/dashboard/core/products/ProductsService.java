package it.pagopa.selfcare.dashboard.core.products;

import it.pagopa.selfcare.dashboard.connector.rest.model.products.Product;

import java.util.List;

public interface ProductsService {
    List<Product> getProducts();
}
