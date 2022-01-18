package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.web.security.AuthoritiesRetriever;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Component
class PartyAuthoritiesRetriever implements AuthoritiesRetriever {

    private final PartyConnector partyConnector;


    @Autowired
    public PartyAuthoritiesRetriever(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }


    @Override
    public Collection<GrantedAuthority> retrieveAuthorities() {
        log.trace("PartyAuthoritiesRetriever.retrieveAuthorities start");
        Collection<GrantedAuthority> authorities = null;

        Collection<AuthInfo> authInfos = partyConnector.getAuthInfo(null);
        if (authInfos != null) {
            authorities = authInfos.stream()
                    .map(authInfo -> new SelfCareGrantedAuthority(authInfo.getInstitutionId(), authInfo.getProductRoles().stream()
                            .map(productRole -> new ProductGrantedAuthority(productRole.getSelfCareRole(),
                                    productRole.getProductRole(),
                                    productRole.getProductId()))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList());
        }

        log.debug("retrieved authorities = {}", authorities);
        log.trace("PartyAuthoritiesRetriever.retrieveAuthorities end");
        return authorities;
    }

}