package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.web.model.ExchangedToken;
import it.pagopa.selfcare.dashboard.web.model.IdentityTokenResource;
import it.pagopa.selfcare.dashboard.web.security.ExchangeTokenService;
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
@RequestMapping(value = "/v1/token", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "token")
public class TokenController {

    private final ExchangeTokenService exchangeTokenService;


    @Autowired
    public TokenController(ExchangeTokenService exchangeTokenService) {
        this.exchangeTokenService = exchangeTokenService;
    }


    @GetMapping(value = "exchange")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.token.api.exchange}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.ProductAclDomain(#institutionId, #productId), 'ANY')")
    public IdentityTokenResource exchange(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                          @RequestParam("institutionId")
                                          String institutionId,
                                          @ApiParam("${swagger.dashboard.products.model.id}")
                                          @RequestParam("productId")
                                          String productId,
                                          @ApiParam("${swagger.dashboard.product-backoffice-configurations.model.environment}")
                                          @RequestParam(value = "environment", required = false)
                                          Optional<String> environment) {

        log.trace("exchange start");
        log.debug("exchange institutionId = {}, productId = {}", institutionId, productId);

        String token = exchangeTokenService.exchange(institutionId, productId, environment).getIdentityToken();
        IdentityTokenResource identityToken = new IdentityTokenResource();
        identityToken.setToken(token);


        log.debug(LogUtils.CONFIDENTIAL_MARKER, "exchange result = {}", identityToken);
        log.trace("exchange end");

        return identityToken;
    }

    @GetMapping(value = "exchange/fatturazione")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.token.api.billingToken}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
    public URI billingToken(@ApiParam("${swagger.dashboard.institutions.model.id}")
                            @RequestParam("institutionId")
                            String institutionId,
                            @ApiParam("${swagger.dashboard.product-backoffice-configurations.model.environment}")
                            @RequestParam(value = "environment", required = false)
                            Optional<String> environment) {

        log.trace("billing exchange start");
        log.debug("billing exchange institutionId = {}", institutionId);
        log.info("env parameter: {}", environment);
        final ExchangedToken exchangedToken = exchangeTokenService.retrieveBillingExchangedToken(institutionId);
        final URI location = URI.create(exchangedToken.getBackOfficeUrl().replace("<IdentityToken>", exchangedToken.getIdentityToken()));
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "billing exchange result = {}", location);
        log.trace("billing exchange end");

        return location;
    }

}
