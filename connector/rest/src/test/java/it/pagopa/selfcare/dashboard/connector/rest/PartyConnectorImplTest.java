package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

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
    void getInstitutionInfo() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.InstitutionInfo instInfo =
                TestUtils.mockInstance(new it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.InstitutionInfo());
        onBoardingInfo.setInstitutions(Collections.singletonList(instInfo));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getInstitutionInfo(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertEquals(instInfo.getDescription(), institutionInfo.getDescription());
        assertEquals(instInfo.getDigitalAddress(), institutionInfo.getDigitalAddress());
        assertEquals(instInfo.getInstitutionId(), institutionInfo.getInstitutionId());
        assertEquals(instInfo.getPlatformRole(), institutionInfo.getPlatformRole());
        assertEquals(instInfo.getRole(), institutionInfo.getRole());
        assertEquals(instInfo.getStatus(), institutionInfo.getStatus());
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
    void getAuthInfo() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.InstitutionInfo instInfo =
                TestUtils.mockInstance(new it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.InstitutionInfo());
        onBoardingInfo.setInstitutions(Collections.singletonList(instInfo));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        AuthInfo authInfo = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfo);
        assertEquals(instInfo.getPlatformRole(), authInfo.getRole());
//        assertNotNull(authInfo.getProducts());//TODO: test after swagger update
//        assertFalse(authInfo.getProducts().isEmpty());//TODO: test after swagger update
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }
}