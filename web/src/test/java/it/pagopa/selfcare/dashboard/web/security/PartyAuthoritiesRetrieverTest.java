package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.auth.ProductRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PartyAuthoritiesRetrieverTest {

    @Mock
    private PartyConnector partyConnectorMock;

    @InjectMocks
    private PartyAuthoritiesRetriever authoritiesRetriever;


    @Test
    void retrieveAuthorities_nullAuthInfo() {
        // given
        Mockito.when(partyConnectorMock.getAuthInfo(Mockito.any()))
                .thenReturn(null);
        // when
        Collection<GrantedAuthority> authorities = authoritiesRetriever.retrieveAuthorities();
        // then
        assertNull(authorities);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getAuthInfo(Mockito.isNull());
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }


    @Test
    void retrieveAuthorities_emptyAuthInfo() {
        // given
        Mockito.when(partyConnectorMock.getAuthInfo(Mockito.any()))
                .thenReturn(Collections.emptyList());
        // when
        Collection<GrantedAuthority> authorities = authoritiesRetriever.retrieveAuthorities();
        // then
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getAuthInfo(Mockito.isNull());
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }


    @Test
    void retrieveAuthorities_notEmptyAuthInfoAndEmptyProductsRole() {
        // given
        Mockito.when(partyConnectorMock.getAuthInfo(Mockito.any()))
                .thenReturn(Collections.singletonList(new AuthInfo() {
                }));
        // when
        Executable executable = () -> authoritiesRetriever.retrieveAuthorities();
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Institution id is required", e.getMessage());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getAuthInfo(Mockito.isNull());
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }


    @Test
    void retrieveAuthorities() {
        // given
        SelfCareAuthority role = ADMIN;
        String institutionId = "institutionId";
        Mockito.when(partyConnectorMock.getAuthInfo(Mockito.any()))
                .thenReturn(List.of(new AuthInfo() {
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
                                return "productRole1";
                            }

                            @Override
                            public String getProductId() {
                                return "productId1";
                            }
                        });
                    }
                }, new AuthInfo() {
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
                                return "productRole2";
                            }

                            @Override
                            public String getProductId() {
                                return "productId2";
                            }
                        });
                    }
                }));
        // when
        Collection<GrantedAuthority> authorities = authoritiesRetriever.retrieveAuthorities();
        // then
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        authorities.forEach(grantedAuthority -> {
            assertEquals(institutionId, ((SelfCareGrantedAuthority) grantedAuthority).getInstitutionId());
            assertEquals(role.name(), grantedAuthority.getAuthority());
        });
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getAuthInfo(Mockito.isNull());
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }

}