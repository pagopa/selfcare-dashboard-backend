package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthenticationDetails;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PartyAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final PartyConnector partyConnector;


    @Autowired
    public PartyAuthenticationProvider(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }


    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    }


    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        User user = null;
        Object authenticationDetails = authentication.getDetails();

        if (authenticationDetails != null
                && SelfCareAuthenticationDetails.class.isAssignableFrom(authenticationDetails.getClass())) {
            Collection<AuthInfo> authInfos = partyConnector.getAuthInfo(((SelfCareAuthenticationDetails) authenticationDetails).getInstitutionId());

            if (authInfos != null) {
                List<SelfCareGrantedAuthority> authorities = authInfos.stream()
                        .map(authInfo -> new SelfCareGrantedAuthority(authInfo.getInstitutionId(), authInfo.getProductRoles().stream()
                                .map(productRole -> new ProductGrantedAuthority(productRole.getSelfCareRole(),
                                        productRole.getProductRole(),
                                        productRole.getProductId()))
                                .collect(Collectors.toList())))
                        .collect(Collectors.toList());
                user = new User(username, authentication.getCredentials().toString(), authorities);
            }
        }

        return user;
    }

}