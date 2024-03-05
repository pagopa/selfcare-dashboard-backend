package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.core.BrokerService;
import it.pagopa.selfcare.dashboard.core.ProductService;
import it.pagopa.selfcare.dashboard.web.model.ExchangedToken;
import it.pagopa.selfcare.dashboard.web.model.mapper.BrokerResourceMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.ProductsMapper;
import it.pagopa.selfcare.dashboard.web.model.product.BrokerResource;
import it.pagopa.selfcare.dashboard.web.model.product.ProductRoleMappingsResource;
import it.pagopa.selfcare.dashboard.web.security.ExchangeTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/v1/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "products")
public class ProductController {

    private final ProductService productService;
    private final ExchangeTokenService exchangeTokenService;
    private final BrokerService brokerService;
    private final BrokerResourceMapper brokerResourceMapper;
    private static final String PAGO_PA_PRODUCT_ID = "prod-pagopa";

    @Autowired
    public ProductController(ProductService productService,
                             ExchangeTokenService exchangeTokenService,
                             BrokerService brokerService,
                             BrokerResourceMapper brokerResourceMapper) {
        this.productService = productService;
        this.exchangeTokenService = exchangeTokenService;
        this.brokerService = brokerService;
        this.brokerResourceMapper = brokerResourceMapper;
    }

    @GetMapping(value = "/{productId}/roles")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.product.api.getProductRoles}")
    public Collection<ProductRoleMappingsResource> getProductRoles(@ApiParam("${swagger.dashboard.products.model.id}")
                                                                   @PathVariable("productId")
                                                                   String productId) {
        log.trace("getProductRoles start");
        log.debug("productId = {}", productId);
        Collection<ProductRoleMappingsResource> result = ProductsMapper.toProductRoleMappingsResource(productService.getProductRoles(productId));
        log.debug("getProductRoles result = {}", result);
        log.trace("getProductRoles end");

        return result;
    }

    @GetMapping(value = "/{productId}/back-office")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.product.api.retrieveProductBackoffice}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.ProductAclDomain(#institutionId, #productId), 'ANY')")
    public URI retrieveProductBackoffice(@ApiParam("${swagger.dashboard.products.model.id}")
                                         @PathVariable("productId")
                                         String productId,
                                         @ApiParam("${swagger.dashboard.institutions.model.id}")
                                         @RequestParam("institutionId")
                                         String institutionId,
                                         @ApiParam("${swagger.dashboard.product-backoffice-configurations.model.environment}")
                                         @RequestParam(value = "environment", required = false)
                                         Optional<String> environment) {
        log.trace("accessProductBackoffice start");
        log.debug("accessProductBackoffice institutionId = {}, productId = {}", institutionId, productId);
        final ExchangedToken exchangedToken = exchangeTokenService.exchange(institutionId, productId, environment);
        final URI location = URI.create(exchangedToken.getBackOfficeUrl().replace("<IdentityToken>", exchangedToken.getIdentityToken()));
        log.trace("accessProductBackoffice end");
        return location;

    }

    @GetMapping(value = "/{productId}/brokers/{institutionType}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.product.api.getProductBrokers}")
    public Collection<BrokerResource> getProductBrokers(@ApiParam("${swagger.dashboard.products.model.id}")
                                                        @PathVariable("productId")
                                                        String productId,
                                                        @ApiParam("${swagger.dashboard.products.model.institutionType}")
                                                        @PathVariable("institutionType")
                                                        InstitutionType institutionType) {
        log.trace("getProductBrokers start");
        log.debug("productId = {}, institutionType = {}", productId, institutionType);
        List<BrokerInfo> brokers;
        if(PAGO_PA_PRODUCT_ID.equals(productId)) {
            brokers = brokerService.findAllByInstitutionType(institutionType.name());
        } else {
            brokers = brokerService.findInstitutionsByProductAndType(productId, institutionType.name());
        }
        Collection<BrokerResource>  result = brokerResourceMapper.toResourceList(brokers);
        log.debug("getProductBrokers result = {}", result);
        log.trace("getProductBrokers end");
        return result;
    }

}
