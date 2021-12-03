package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.dashboard.web.model.IdentityTokenResource;
import it.pagopa.selfcare.dashboard.web.security.ExchangeTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "token", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "token")
public class TokenController {

    private final ExchangeTokenService exchangeTokenService;


    @Autowired
    public TokenController(ExchangeTokenService exchangeTokenService) {
        this.exchangeTokenService = exchangeTokenService;
    }


    @GetMapping(value = "exchange")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#productId, 'ProductsResource', null)")
    public IdentityTokenResource exchange(@ApiParam("${swagger.dashboard.products.model.id}")
                                          @RequestParam("productId")
                                                  String productId,
                                          @ApiParam(value = "${swagger.dashboard.token.model.realm}", example = "io.selfcare.pagopa.it", defaultValue = "defVal")
                                          @RequestParam("realm")
                                                  String realm) {
        if (log.isDebugEnabled()) {
            log.trace("TokenController.exchange");
            log.debug("productId = {}, realm = {}", productId, realm);
        }
        String token = exchangeTokenService.exchange(productId, realm);
        IdentityTokenResource identityToken = new IdentityTokenResource();
        identityToken.setToken(token);

        return identityToken;
    }

}
