package it.pagopa.selfcare.dashboard.service;

import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Map;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final it.pagopa.selfcare.product.service.ProductService productService;

    @Autowired
    ProductServiceImpl(it.pagopa.selfcare.product.service.ProductService productService) {
        this.productService = productService;
    }


    @Override
    public Map<PartyRole, ProductRoleInfo> getProductRoles(String productId, String institutionType) {
        log.trace("getProductRoles start");
        log.debug("getProductRoles productId = {}, institutionType = {}", Encode.forJava(productId), Encode.forJava(institutionType));
        Assert.hasText(productId, "A Product id is required");
        Product product = productService.getProduct(productId);
        Map<PartyRole, ProductRoleInfo> productRoleMappings = product != null ? product.getRoleMappings(institutionType) : null;

        log.debug("getProductRoles result = {}", productRoleMappings);
        log.trace("getProductRoles end");
        return productRoleMappings;
    }

}
