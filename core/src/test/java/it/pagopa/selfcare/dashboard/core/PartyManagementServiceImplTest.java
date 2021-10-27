package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.rest.client.PartyManagementRestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PartyManagementServiceImplTest {

    @InjectMocks
    private PartyManagementServiceImpl partyManagementService;

    @Mock
    private PartyManagementRestClient restClientMock;


    @Test
    void getOrganization() {
        // when
        partyManagementService.getOrganization(null);
        // then
        // verifica che il metodo getOrganization del restClientMock sia stato invocato una volta
        Mockito.verify(restClientMock, Mockito.times(1)).getOrganization(null);
    }
}