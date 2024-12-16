import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.web.security.JwtService;
import it.pagopa.selfcare.dashboard.client.UserInstitutionApiRestClient;
import it.pagopa.selfcare.dashboard.config.ExchangeTokenProperties;
import it.pagopa.selfcare.dashboard.model.mapper.InstitutionMapper;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserInstitutionResponse;
import it.pagopa.selfcare.dashboard.model.mapper.InstitutionMapperImpl;
import it.pagopa.selfcare.dashboard.model.mapper.InstitutionResourceMapperImpl;
import it.pagopa.selfcare.dashboard.model.product.mapper.ProductMapper;
import it.pagopa.selfcare.dashboard.model.user.UserInstitution;
import it.pagopa.selfcare.dashboard.security.ExchangeTokenServiceV2;
import it.pagopa.selfcare.dashboard.service.InstitutionService;
import it.pagopa.selfcare.dashboard.service.UserGroupV2Service;
import it.pagopa.selfcare.dashboard.service.UserV2Service;
import it.pagopa.selfcare.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeTokenServiceV2Test {

    @Mock
    private JwtService jwtService;
    @Mock
    private InstitutionService institutionService;
    @Mock
    private UserGroupV2Service groupService;
    @Mock
    private UserV2Service userService;
    @Mock
    private ProductService productService;
    @Mock
    private UserInstitutionApiRestClient userInstitutionApiRestClient;
    @Spy
    private InstitutionResourceMapperImpl institutionResourceMapper;
    @Spy
    private InstitutionMapperImpl institutionMapper;
    @Spy
    private ProductMapper productMapper;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ExchangeTokenServiceV2 exchangeTokenServiceV2;

    @Spy
    private ExchangeTokenProperties exchangeTokenProperties;

    @BeforeEach
    void setUp() {
        when(exchangeTokenProperties.getBillingAudience()).thenReturn("billingAudience");
        when(exchangeTokenProperties.getBillingUrl()).thenReturn("billingUrl");
        when(exchangeTokenProperties.getDuration()).thenReturn("duration");
        when(exchangeTokenProperties.getIssuer()).thenReturn("issuer");
        when(exchangeTokenProperties.getKid()).thenReturn("kid");
        when(exchangeTokenProperties.getSigningKey()).thenReturn("signingKey");
    }

    @Test
    void exchangeThrowsExceptionWhenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            exchangeTokenServiceV2.exchange("institutionId", "productId", Optional.empty());
        });

        assertEquals("Authentication is required", exception.getMessage());
    }

    @Test
    void exchangeThrowsExceptionWhenProductGrantedAuthorityIsNull() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("userId").build());
        when(authentication.getCredentials()).thenReturn("credentials");
        when(userInstitutionApiRestClient._retrieveUserInstitutions(anyString(), any(), any(), any(), any(), anyString()))
                .thenReturn(ResponseEntity.ok(List.of(new UserInstitutionResponse())));
        when(institutionMapper.toInstitution(any(UserInstitutionResponse.class))).thenReturn(new UserInstitution());
        when(institutionService.getInstitutionById(anyString())).thenReturn(new it.pagopa.selfcare.dashboard.model.institution.Institution());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exchangeTokenServiceV2.exchange("institutionId", "productId", Optional.empty());
        });

        assertEquals("A Product Granted SelfCareAuthority is required for product 'productId' and institution 'institutionId'", exception.getMessage());
    }

    @Test
    void retrieveBillingExchangedTokenThrowsExceptionWhenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            exchangeTokenServiceV2.retrieveBillingExchangedToken("institutionId");
        });

        assertEquals("Authentication is required", exception.getMessage());
    }

    @Test
    void retrieveBillingExchangedTokenThrowsExceptionWhenInstitutionInfoIsNull() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("userId").build());
        when(authentication.getCredentials()).thenReturn("credentials");
        when(userInstitutionApiRestClient._retrieveUserInstitutions(anyString(), any(), any(), any(), any(), anyString()))
                .thenReturn(ResponseEntity.ok(List.of(new UserInstitutionResponse())));
        when(institutionMapper.toInstitution(any(UserInstitutionResponse.class))).thenReturn(new UserInstitution());
        when(institutionService.getInstitutionById(anyString())).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exchangeTokenServiceV2.retrieveBillingExchangedToken("institutionId");
        });

        assertEquals("Institution info is required", exception.getMessage());
    }
}