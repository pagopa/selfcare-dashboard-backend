package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.connector.model.product.mapper.ProductMapper;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;
import it.pagopa.selfcare.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProductConnectorImpl implements ProductsConnector {

    private final ProductService productService;

    private final ProductMapper productMapper;

    public ProductConnectorImpl(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @Override
    public List<Product> getProducts() {
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getProducts");
        return productService.getProducts(false,true);
    }

    @Override
    public Map<PartyRole, ProductRoleInfo> getProductRoleMappings(String productId) {
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getProduct productId = {}", productId);
        Assert.hasText(productId, "A productId is required");
        Product product = productService.getProduct(productId);
        Map<PartyRole, ProductRoleInfo> result = product != null ? product.getRoleMappings() : null;
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getProduct result = {}", result);
        return result;
    }

    @Override
    public Product getProduct(String productId) {
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getProduct productId = {}", productId);
        Assert.hasText(productId, "A productId is required");
        Product result = productService.getProduct(productId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getProduct result = {}", result);
        return result;
    }

    @Override
    public List<ProductTree> getProductsTree() {
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getProductsTree");
        List<Product> products = productService.getProducts(false, true);
        return productMapper.toTreeResource(products);
    }
}
