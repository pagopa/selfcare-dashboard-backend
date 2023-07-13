package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.PagoPABackOfficeConnector;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class BrokerServiceImplTest {

    @Mock
    private PagoPABackOfficeConnector backOfficeConnectorMock;

    @InjectMocks
    private BrokerServiceImpl brokerService;

    @Test
    void getBrokersEC() {
        // given
        String institutionType = "EC";
        List<BrokerInfo> brokersMocked = buildBrokerInfos();
        Mockito.when(backOfficeConnectorMock.getBrokersEC(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(brokersMocked);
        // when
        List<BrokerInfo> brokers = brokerService.findAllByInstitutionType(institutionType);
        // then
        Assertions.assertSame(brokersMocked, brokers);
        Mockito.verify(backOfficeConnectorMock, Mockito.times(1))
                .getBrokersEC(1,1000);
        Mockito.verifyNoMoreInteractions(backOfficeConnectorMock);

    }

    @Test
    void getBrokersPSP() {
        // given
        String institutionType = "PSP";
        List<BrokerInfo> brokersMocked = buildBrokerInfos();
        Mockito.when(backOfficeConnectorMock.getBrokersPSP(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(brokersMocked);
        // when
        List<BrokerInfo> brokers = brokerService.findAllByInstitutionType(institutionType);
        // then
        Assertions.assertSame(brokersMocked, brokers);
        Mockito.verify(backOfficeConnectorMock, Mockito.times(1))
                .getBrokersPSP(1,1000);
        Mockito.verifyNoMoreInteractions(backOfficeConnectorMock);

    }

    private List<BrokerInfo> buildBrokerInfos() {
        BrokerInfo brokerInfo = new BrokerInfo();
        brokerInfo.setBrokerCode("code");
        List<BrokerInfo> brokersMocked = new ArrayList<>();
        brokersMocked.add(brokerInfo);
        return brokersMocked;
    }

}
