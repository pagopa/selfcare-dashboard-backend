package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Map;

@Slf4j
@Service
class ProductServiceImpl implements ProductService {

    private final ProductsConnector productsConnector;


    @Autowired
    ProductServiceImpl(ProductsConnector productsConnector) {
        this.productsConnector = productsConnector;
    }


    @Override
    public Map<PartyRole, it.pagopa.selfcare.product.entity.ProductRoleInfo> getProductRoles(String productId) {
        log.trace("getProductRoles start");
        log.debug("getProductRoles productId = {}", productId);
        Assert.hasText(productId, "A Product id is required");

        Map<PartyRole, ProductRoleInfo> productRoleMappings = productsConnector.getProductRoleMappings(productId);
        log.debug("getProductRoles result = {}", productRoleMappings);
        log.trace("getProductRoles end");
        return productRoleMappings;
    }

}
