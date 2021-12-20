package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.auth.ProductRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PartyAuthenticationProviderTest {

    @Mock
    private PartyConnector partyConnectorMock;

    @InjectMocks
    private PartyAuthenticationProvider authenticationProvider;


    @Test
    void retrieveUser_nullAuthInfo() {
        // given
        String username = "username";
        String credentials = "credentials";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, credentials);
        Mockito.when(partyConnectorMock.getAuthInfo(Mockito.any()))
                .thenReturn(null);
        // when
        UserDetails userDetails = authenticationProvider.retrieveUser(username, authentication);
        // then
        assertNull(userDetails);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getAuthInfo(Mockito.isNull());
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }


    @Test
    void retrieveUser_emptyAuthInfo() {
        // given
        String username = "username";
        String credentials = "credentials";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, credentials);
        Mockito.when(partyConnectorMock.getAuthInfo(Mockito.any()))
                .thenReturn(Collections.emptyList());
        // when
        UserDetails userDetails = authenticationProvider.retrieveUser(username, authentication);
        // then
        assertNotNull(userDetails);
        assertNotNull(userDetails.getAuthorities());
        assertTrue(userDetails.getAuthorities().isEmpty());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getAuthInfo(Mockito.isNull());
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }


    @Test
    void retrieveUser_notEmptyAuthInfoAndEmptyProductsRole() {
        // given
        String username = "username";
        String credentials = "credentials";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, credentials);
        Mockito.when(partyConnectorMock.getAuthInfo(Mockito.any()))
                .thenReturn(Collections.singletonList(new AuthInfo() {
                }));
        // when
        Executable executable = () -> authenticationProvider.retrieveUser(username, authentication);
        // then
        assertThrows(IllegalArgumentException.class, executable);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getAuthInfo(Mockito.isNull());
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }


    @Test
    void retrieveUser() {
        // given
        SelfCareAuthority role = ADMIN;
        String username = "username";
        String credentials = "credentials";
        String institutionId = "institutionId";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, credentials);
        Mockito.when(partyConnectorMock.getAuthInfo(Mockito.any()))
                .thenReturn(Collections.singletonList(new AuthInfo() {
                    @Override
                    public String getInstitutionId() {
                        return institutionId;
                    }

                    @Override
                    public Collection<ProductRole> getProductRoles() {
                        return Collections.singleton(new ProductRole() {
                            @Override
                            public SelfCareAuthority getSelfCareRole() {
                                return role;
                            }

                            @Override
                            public String getProductRole() {
                                return "productRole";
                            }

                            @Override
                            public String getProductId() {
                                return "productId";
                            }
                        });
                    }
                }));
        // when
        UserDetails userDetails = authenticationProvider.retrieveUser(username, authentication);
        // then
        assertNotNull(userDetails);
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
        Optional<? extends GrantedAuthority> grantedAuthority = userDetails.getAuthorities().stream()
                .filter(authority -> SelfCareGrantedAuthority.class.isAssignableFrom(authority.getClass()))
                .findAny();
        assertTrue(grantedAuthority.isPresent());
        SelfCareGrantedAuthority selfCareGrantedAuthority = (SelfCareGrantedAuthority) grantedAuthority.get();
        assertEquals(institutionId, selfCareGrantedAuthority.getInstitutionId());
        assertEquals(role.name(), selfCareGrantedAuthority.getAuthority());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getAuthInfo(Mockito.isNull());
        Mockito.verifyNoMoreInteractions(partyConnectorMock);


    }

}