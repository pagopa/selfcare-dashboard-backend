package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PartyConnectorImplTest {

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
    void getInstitutionInfo_emptyInstitutionInfos() {
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
    void getInstitutionInfo_nullInstitutionProduct() {
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
        assertNotNull(institutionInfo.getActiveProducts());
        assertTrue(institutionInfo.getActiveProducts().isEmpty());
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
        onboardingData.setInstitutionProducts(List.of("prod_1"));
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
        assertNotNull(institutionInfo.getActiveProducts());
        assertIterableEquals(onboardingData.getInstitutionProducts(), institutionInfo.getActiveProducts());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
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
    void getAuthInfo_emptyInstitutionInfos() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        onBoardingInfo.setInstitutions(Collections.emptyList());
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
    void getAuthInfo_nullRelationshipProducts() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        onBoardingInfo.setInstitutions(Collections.singletonList(onboardingData));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        AuthInfo authInfo = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfo);
        assertNull(authInfo.getProducts());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        onboardingData.setRelationshipProducts(List.of("prod_1"));
        onBoardingInfo.setInstitutions(Collections.singletonList(onboardingData));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        AuthInfo authInfo = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfo);
        assertEquals(onboardingData.getProductRole(), authInfo.getRole());
        assertNotNull(authInfo.getProducts());
        assertIterableEquals(onboardingData.getRelationshipProducts(), authInfo.getProducts());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

}