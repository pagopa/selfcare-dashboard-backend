package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;

import java.util.List;
import java.util.Map;

public interface ProductsConnector {

    List<Product> getProducts();

    Map<PartyRole, ProductRoleInfo> getProductRoleMappings(String productId);

    Product getProduct(String productId);

    List<ProductTree> getProductsTree();

}
