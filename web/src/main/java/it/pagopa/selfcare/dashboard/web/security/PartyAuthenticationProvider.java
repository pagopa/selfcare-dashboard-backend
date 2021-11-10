package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthenticationDetails;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.OnBoardingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

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
            OnBoardingInfo onBoardingInfo = partyConnector.getOnBoardingInfo(((SelfCareAuthenticationDetails) authenticationDetails).getInstitutionId());

            if (onBoardingInfo != null && !onBoardingInfo.getInstitutions().isEmpty()) {
                InstitutionInfo institutionInfo = onBoardingInfo.getInstitutions().get(0);
                String role = institutionInfo.getPlatformRole();
                List<SelfCareGrantedAuthority> authorities = Collections.singletonList(new SelfCareGrantedAuthority(role));
                user = new User(username, authentication.getCredentials().toString(), authorities);
            }
        }

        return user;
    }

}