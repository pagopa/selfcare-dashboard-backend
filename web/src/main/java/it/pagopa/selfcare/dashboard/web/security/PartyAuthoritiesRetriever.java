package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.web.security.AuthoritiesRetriever;
import it.pagopa.selfcare.commons.web.security.AuthoritiesRetrieverException;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(
        value="dashboard.security.connector",
        havingValue = "v1",
        matchIfMissing = true)
class PartyAuthoritiesRetriever implements AuthoritiesRetriever {

    private final MsCoreConnector msCoreConnector;


    @Autowired
    public PartyAuthoritiesRetriever(MsCoreConnector msCoreConnector) {
        this.msCoreConnector = msCoreConnector;
    }


    @Override
    public Collection<GrantedAuthority> retrieveAuthorities() {
        try {
            log.trace("retrieveAuthorities start");
            Collection<GrantedAuthority> authorities = null;

            Collection<AuthInfo> authInfos = msCoreConnector.getAuthInfo(null);

            if (authInfos != null) {
                authorities = authInfos.stream()
                        .map(authInfo -> new SelfCareGrantedAuthority(authInfo.getInstitutionId(), authInfo.getProductRoles().stream()
                                .map(productRole -> new ProductGrantedAuthority(productRole.getPartyRole(),
                                        productRole.getProductRole(),
                                        productRole.getProductId()))
                                .collect(Collectors.toMap(ProductGrantedAuthority::getProductId,
                                        Function.identity(),
                                        ProductGrantedAuthority.MERGE))
                                .values()))
                        .collect(Collectors.toList());
            }

            log.debug(LogUtils.CONFIDENTIAL_MARKER, "retrieved authorities = {}", authorities);
            log.trace("retrieveAuthorities end");
            return authorities;
        } catch (AuthoritiesRetrieverException e){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            SelfCareUser selfCareUser = (SelfCareUser) authentication.getPrincipal();
            log.error("retrieveAuthorities error for user {}, error message: {}", selfCareUser.getId(), e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

}