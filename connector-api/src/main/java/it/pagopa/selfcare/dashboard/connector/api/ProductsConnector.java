package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;

import java.util.List;
import java.util.Map;

public interface ProductsConnector {

    List<Product> getProducts();

    Map<PartyRole, ProductRoleInfo> getProductRoleMappings(String productId);

    Product getProduct(String productId);

    List<ProductTree> getProductsTree();

}
