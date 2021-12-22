package it.pagopa.selfcare.dashboard.web.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductAclDomain {

    private final String institutionId;
    private final String productId;

}
