package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.product.Product;

import java.util.List;

public interface ProductsService {
    List<Product> getProducts();
}
