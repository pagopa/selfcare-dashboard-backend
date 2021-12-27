package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.product.Product;

import java.util.List;
import java.util.Map;

public interface ProductsConnector {

    List<Product> getProducts();

    Map<String, List<String>> getProductRoleMappings(String productId);

}
