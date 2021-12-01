package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {

    @Mock
    private PartyConnector partyConnectorMock;

    @InjectMocks
    private InstitutionServiceImpl institutionService;

    @Test
    void getInstitution() {
        // given
        String institutionId = "institutionId";
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();
        Mockito.when(partyConnectorMock.getInstitutionInfo(Mockito.any()))
                .thenReturn(expectedInstitutionInfo);
        // when
        InstitutionInfo institutionInfo = institutionService.getInstitution(institutionId);
        // then
        assertSame(expectedInstitutionInfo, institutionInfo);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getInstitutionInfo(institutionId);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }

}