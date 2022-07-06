package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;

import java.util.Map;

public interface ProductService {

    Map<PartyRole, ProductRoleInfo> getProductRoles(String productId);

}
