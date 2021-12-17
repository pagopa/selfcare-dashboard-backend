package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

class ProductServiceImpl implements ProductService {

    private static final Set<String> PARTY_ROLE_WHITE_LIST = Set.of("SUB_DELEGATE", "OPERATORS");

    private final ProductsConnector productsConnector;


    @Autowired
    ProductServiceImpl(ProductsConnector productsConnector) {
        this.productsConnector = productsConnector;
    }


    @Override
    public Collection<String> getProductRoles(String productId) {
        Assert.hasText(productId, "A Product id is required");

        return productsConnector.getProductRoleMappings(productId).entrySet().stream()
                .filter(entry -> PARTY_ROLE_WHITE_LIST.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
    }

}
