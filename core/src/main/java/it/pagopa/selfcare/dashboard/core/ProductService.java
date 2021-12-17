package it.pagopa.selfcare.dashboard.core;

import java.util.Collection;

public interface ProductService {

    Collection<String> getProductRoles(String productId);

}
