package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.dashboard.core.ProductService;
import it.pagopa.selfcare.dashboard.web.model.ExchangedToken;
import it.pagopa.selfcare.dashboard.web.model.mapper.ProductsMapper;
import it.pagopa.selfcare.dashboard.web.model.product.ProductRoleMappingsResource;
import it.pagopa.selfcare.dashboard.web.security.ExchangeTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "products")
public class ProductController {

    private final ProductService productService;
    private final ExchangeTokenService exchangeTokenService;


    @Autowired
    public ProductController(ProductService productService, ExchangeTokenService exchangeTokenService) {
        this.productService = productService;
        this.exchangeTokenService = exchangeTokenService;
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
    @ResponseStatus(HttpStatus.SEE_OTHER)
    @ApiOperation(value = "", notes = "${swagger.dashboard.product.api.retrieveProductBackoffice}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.ProductAclDomain(#institutionId, #productId), 'ANY')")
    public ResponseEntity<Void> retrieveProductBackoffice(@ApiParam("${swagger.dashboard.products.model.id}")
                                                          @PathVariable("productId")
                                                                  String productId,
                                                          @ApiParam("${swagger.dashboard.institutions.model.id}")
                                                          @RequestParam("institutionId")
                                                                  String institutionId) {
        log.trace("accessProductBackoffice start");
        log.debug("accessProductBackoffice institutionId = {}, productId = {}", institutionId, productId);
        final ExchangedToken exchangedToken = exchangeTokenService.exchange(institutionId, productId);
        final URI location = URI.create(exchangedToken.getBackOfficeUrl().replace("<IdentityToken>", exchangedToken.getIdentityToken()));
        log.trace("accessProductBackoffice end");
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(location).build();
    }

}
