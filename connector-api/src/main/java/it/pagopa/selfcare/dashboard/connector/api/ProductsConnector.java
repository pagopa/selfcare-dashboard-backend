package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.product.Product;

import java.util.List;

public interface ProductsConnector {

    List<Product> getProducts();
}
