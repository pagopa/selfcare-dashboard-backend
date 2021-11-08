package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthenticationDetails;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.process.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.process.OnBoardingInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PartyAuthenticationProviderTest {

    @Mock
    private PartyProcessRestClient restClientMock;

    @InjectMocks
    private PartyAuthenticationProvider authenticationProvider;


    @Test
    void retrieveUser_nullAuthDetails() {
        // given
        String username = "username";
        String credentials = "credentials";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, credentials);
        // when
        UserDetails userDetails = authenticationProvider.retrieveUser(username, authentication);
        // then
        assertNull(userDetails);
    }


    @Test
    void retrieveUser_nullOnBoardingInfo() {
        // given
        String username = "username";
        String credentials = "credentials";
        String institutionId = "institutionId";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, credentials);
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        // when
        UserDetails userDetails = authenticationProvider.retrieveUser(username, authentication);
        // then
        assertNull(userDetails);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void retrieveUser_emptyInstitutions() {
        // given
        String username = "username";
        String credentials = "credentials";
        String institutionId = "institutionId";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, credentials);
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        onBoardingInfo.setInstitutions(Collections.emptyList());
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        UserDetails userDetails = authenticationProvider.retrieveUser(username, authentication);
        // then
        assertNull(userDetails);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void retrieveUser() {
        // given
        String username = "username";
        String credentials = "credentials";
        String institutionId = "institutionId";
        String role = "ADMIN";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, credentials);
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        InstitutionInfo institutionInfo = new InstitutionInfo();
        institutionInfo.setPlatformRole(role);
        onBoardingInfo.setInstitutions(Collections.singletonList(institutionInfo));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        UserDetails userDetails = authenticationProvider.retrieveUser(username, authentication);
        // then
        assertNotNull(userDetails);
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
        Optional<? extends GrantedAuthority> grantedAuthority = userDetails.getAuthorities().stream().findAny();
        assertTrue(grantedAuthority.isPresent());
        assertEquals(role, grantedAuthority.get().getAuthority());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);


    }

}