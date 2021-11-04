package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.web.security.JwtAuthenticationDetails;
import it.pagopa.selfcare.commons.web.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.process.RelationshipInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.process.RelationshipsResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;


public class PartyAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final PartyProcessRestClient restClient;


    public PartyAuthenticationProvider(PartyProcessRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    }


    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        // TODO: implement real logic
//        RelationshipsResponse institutionRelationships = restClient.getInstitutionRelationships(details.getInstitutionId());

        // start mock
        RelationshipInfo info = new RelationshipInfo();
        info.setFrom("from");
        info.setStatus(RelationshipInfo.StatusEnum.ACTIVE);
        info.setRole(RelationshipInfo.RoleEnum.MANAGER);
        info.setPlatformRole("ADMIN");
        RelationshipsResponse institutionRelationships = new RelationshipsResponse();
        institutionRelationships.add(info);
        // end mock

        User user = null;
        if (!institutionRelationships.isEmpty()) {
            RelationshipInfo relationshipInfo = institutionRelationships.get(0);
            String role = relationshipInfo.getPlatformRole();
            user = new User(username, authentication.getCredentials().toString(), Collections.singletonList(new SelfCareGrantedAuthority(role)));
        }
        // TODO: map RelationshipsResponse to UserDetails
//        return new User("admin", "", Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_ADMIN.name())));// TODO: implement real logic
        return user;
    }

}