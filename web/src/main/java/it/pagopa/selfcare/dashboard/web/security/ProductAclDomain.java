package it.pagopa.selfcare.dashboard.web.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class ProductAclDomain {

    private final String institutionId;
    private final String productId;

}
