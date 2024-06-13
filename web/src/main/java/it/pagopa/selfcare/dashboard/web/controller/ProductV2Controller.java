package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.dashboard.web.model.ExchangedToken;
import it.pagopa.selfcare.dashboard.web.security.ExchangeTokenServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/v2/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "products")
public class ProductV2Controller {

    private final ExchangeTokenServiceV2 exchangeTokenService;

    @Autowired
    public ProductV2Controller(ExchangeTokenServiceV2 exchangeTokenServiceV2) {
        this.exchangeTokenService = exchangeTokenServiceV2;
    }

    @GetMapping(value = "/{productId}/back-office", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "${swagger.dashboard.product.api.retrieveProductBackoffice}", notes = "${swagger.dashboard.product.api.retrieveProductBackoffice}", nickname = "v2RetrieveProductBackofficeUsingGET")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.ProductAclDomain(#institutionId, #productId), 'ANY')")
    public URI retrieveProductBackoffice(@ApiParam("${swagger.dashboard.products.model.id}")
                                         @PathVariable("productId")
                                         String productId,
                                         @ApiParam("${swagger.dashboard.institutions.model.id}")
                                         @RequestParam("institutionId")
                                         String institutionId,
                                         @ApiParam("${swagger.dashboard.product-backoffice-configurations.model.environment}")
                                         @RequestParam(value = "environment", required = false)
                                         Optional<String> environment,
                                         @ApiParam("${swagger.dashboard.product-backoffice-configurations.model.lang}")
                                         @RequestParam(value = "lang", required = false, defaultValue = "it")
                                         String lang) {
        log.trace("accessProductBackoffice start");
        log.debug("accessProductBackoffice institutionId = {}, productId = {}", institutionId, productId);
        final ExchangedToken exchangedToken = exchangeTokenService.exchange(institutionId, productId, environment);
        final URI location = URI.create(exchangedToken.getBackOfficeUrl()
                .replace("<IdentityToken>", exchangedToken.getIdentityToken())
                .replace("<lang>", lang));
        log.trace("accessProductBackoffice end");
        return location;

    }
}
