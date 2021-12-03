package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.auth.ProductRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PartyConnectorImplTest {

    private static final EnumMap<PartyRole, SelfCareAuthority> PARTY_ROLE_AUTHORITY_MAP = new EnumMap<>(PartyRole.class) {{
        put(PartyRole.MANAGER, SelfCareAuthority.ADMIN);
        put(PartyRole.DELEGATE, SelfCareAuthority.ADMIN);
        put(PartyRole.SUB_DELEGATE, SelfCareAuthority.ADMIN);
        put(PartyRole.OPERATOR, SelfCareAuthority.LIMITED);
    }};

    @InjectMocks
    private PartyConnectorImpl partyConnector;

    @Mock
    private PartyProcessRestClient restClientMock;


    @Test
    void getInstitutionInfo_nullOnBoardingInfo() {
        // given
        String institutionId = "institutionId";
        // when
        InstitutionInfo institutionInfo = partyConnector.getInstitutionInfo(institutionId);
        // then
        assertNull(institutionInfo);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionInfo_nullInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getInstitutionInfo(institutionId);
        // then
        assertNull(institutionInfo);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getInstitutionInfo_emptyInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        onBoardingInfo.setInstitutions(Collections.emptyList());
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getInstitutionInfo(institutionId);
        // then
        assertNull(institutionInfo);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionInfo_nullAttributes() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        onBoardingInfo.setInstitutions(Collections.singletonList(onboardingData));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getInstitutionInfo(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertNull(institutionInfo.getCategory());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionInfo_emptyAttributes() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        onboardingData.setAttributes(Collections.emptyList());
        onBoardingInfo.setInstitutions(Collections.singletonList(onboardingData));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getInstitutionInfo(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertNull(institutionInfo.getCategory());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionInfo() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        onboardingData.setAttributes(List.of(TestUtils.mockInstance(new Attribute())));
        onBoardingInfo.setInstitutions(Collections.singletonList(onboardingData));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getInstitutionInfo(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertEquals(onboardingData.getDescription(), institutionInfo.getDescription());
        assertEquals(onboardingData.getDigitalAddress(), institutionInfo.getDigitalAddress());
        assertEquals(onboardingData.getInstitutionId(), institutionInfo.getInstitutionId());
        assertEquals(onboardingData.getState().toString(), institutionInfo.getStatus());
        assertEquals(onboardingData.getAttributes().get(0).getDescription(), institutionInfo.getCategory());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionProducts_nullProducts() {
        // given
        String institutionId = "institutionId";
        // when
        List<String> institutionProducts = partyConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionProducts(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionProducts_nullProductsInfo() {
        // given
        String institutionId = "institutionId";
        Mockito.when(restClientMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(new Products());
        // when
        List<String> institutionProducts = partyConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionProducts(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionProducts_emptyProductsInfo() {
        // given
        String institutionId = "institutionId";
        Products products = new Products();
        products.setProducts(Collections.emptyList());
        Mockito.when(restClientMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(products);
        // when
        List<String> institutionProducts = partyConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionProducts(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionProducts_notEmptyProductsInfo() {
        // given
        String institutionId = "institutionId";
        Products products = new Products();
        products.setProducts(List.of(TestUtils.mockInstance(new ProductInfo())));
        Mockito.when(restClientMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(products);
        // when
        List<String> institutionProducts = partyConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertFalse(institutionProducts.isEmpty());
        assertEquals(products.getProducts().get(0).getId(), institutionProducts.get(0));
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionProducts(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo_nullOnBoardingInfo() {
        // given
        String institutionId = "institutionId";
        // when
        AuthInfo authInfo = partyConnector.getAuthInfo(institutionId);
        // then
        assertNull(authInfo);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo_nullInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        AuthInfo authInfo = partyConnector.getAuthInfo(institutionId);
        // then
        assertNull(authInfo);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo_emptyInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        onBoardingInfo.setInstitutions(Collections.emptyList());
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        AuthInfo authInfo = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfo);
        assertNotNull(authInfo.getProductRoles());
        assertTrue(authInfo.getProductRoles().isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo_nullProductInfo() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData(), "setProductInfo");
        onboardingData.setState(RelationshipState.ACTIVE);
        onBoardingInfo.setInstitutions(List.of(onboardingData));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        AuthInfo authInfo = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfo);
        assertNotNull(authInfo.getProductRoles());
        assertTrue(authInfo.getProductRoles().isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo_notActive() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        onboardingData.setState(RelationshipState.PENDING);
        onBoardingInfo.setInstitutions(List.of(onboardingData));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        AuthInfo authInfo = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfo);
        assertNotNull(authInfo.getProductRoles());
        assertTrue(authInfo.getProductRoles().isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData1 = TestUtils.mockInstance(new OnboardingData(), 1);
        onboardingData1.setState(RelationshipState.ACTIVE);
        OnboardingData onboardingData2 = TestUtils.mockInstance(new OnboardingData(), 2);
        onboardingData2.setState(RelationshipState.PENDING);
        OnboardingData onboardingData3 = TestUtils.mockInstance(new OnboardingData(), 3, "setProductInfo");
        onboardingData3.setState(RelationshipState.ACTIVE);
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2, onboardingData3));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        AuthInfo authInfo = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfo);
        assertNotNull(authInfo.getProductRoles());
        assertEquals(1, authInfo.getProductRoles().size());
        ProductRole productRole = authInfo.getProductRoles().iterator().next();
        assertEquals(onboardingData1.getProductInfo().getId(), productRole.getProductId());
        assertEquals(onboardingData1.getProductInfo().getRole(), productRole.getProductRole());
        assertEquals(PARTY_ROLE_AUTHORITY_MAP.get(onboardingData1.getRole()), productRole.getSelfCareRole());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @ParameterizedTest
    @EnumSource(value = PartyRole.class)
    void getAuthInfo(PartyRole partyRole) {
        // when
        SelfCareAuthority authority = PartyConnectorImpl.PARTY_2_SELC_ROLE.apply(partyRole);
        // then
        assertEquals(PARTY_ROLE_AUTHORITY_MAP.get(partyRole), authority);
    }

}