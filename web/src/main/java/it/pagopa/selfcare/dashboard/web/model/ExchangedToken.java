package it.pagopa.selfcare.dashboard.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExchangedToken {

    private String identityToken;
    private String backOfficeUrl;

}
