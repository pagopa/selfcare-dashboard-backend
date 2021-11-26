package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import it.pagopa.selfcare.dashboard.web.model.IdentityTokenResource;
import it.pagopa.selfcare.dashboard.web.security.ExchangeTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    public IdentityTokenResource exchange(@RequestParam("productCode") String productCode,
                                          @RequestParam("realm") String realm) {
        String token = exchangeTokenService.exchange(productCode, realm);
        IdentityTokenResource identityToken = new IdentityTokenResource();
        identityToken.setToken(token);

        return identityToken;
    }

}
