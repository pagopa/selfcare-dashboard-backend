package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class BrokerServiceImplTest {

    @Mock
    private PagoPABackOfficeConnector backOfficeConnectorMock;

    @Mock
    private MsCoreConnector msCoreConnector;

    @InjectMocks
    private BrokerServiceImpl brokerService;

    @Test
    void getBrokersEC() {
        // given
        String institutionType = "EC";
        List<BrokerInfo> brokersMocked = buildBrokerInfos();
        Mockito.when(backOfficeConnectorMock.getBrokersEC(anyInt(), anyInt()))
                .thenReturn(brokersMocked);
        // when
        List<BrokerInfo> brokers = brokerService.findAllByInstitutionType(institutionType);
        // then
        Assertions.assertSame(brokersMocked, brokers);
        Mockito.verify(backOfficeConnectorMock, Mockito.times(1))
                .getBrokersEC(0,1000);
        Mockito.verifyNoMoreInteractions(backOfficeConnectorMock);

    }

    @Test
    void getBrokersPSP() {
        // given
        String institutionType = "PSP";
        List<BrokerInfo> brokersMocked = buildBrokerInfos();
        Mockito.when(backOfficeConnectorMock.getBrokersPSP(anyInt(), anyInt()))
                .thenReturn(brokersMocked);
        // when
        List<BrokerInfo> brokers = brokerService.findAllByInstitutionType(institutionType);
        // then
        Assertions.assertSame(brokersMocked, brokers);
        Mockito.verify(backOfficeConnectorMock, Mockito.times(1))
                .getBrokersPSP(0,1000);
        Mockito.verifyNoMoreInteractions(backOfficeConnectorMock);

    }

    @Test
    void findInstitutionsByProductAndType() {
        // given
        final String institutionType = "EC";
        final String productId = "productId";
        List<BrokerInfo> brokersMocked = buildBrokerInfos();
        Mockito.when(msCoreConnector.findInstitutionsByProductAndType(anyString(), anyString()))
                .thenReturn(brokersMocked);
        // when
        List<BrokerInfo> brokers = brokerService.findInstitutionsByProductAndType(productId, institutionType);
        // then
        Assertions.assertSame(brokersMocked, brokers);
        Mockito.verify(msCoreConnector, Mockito.times(1))
                .findInstitutionsByProductAndType(productId,institutionType);
        Mockito.verifyNoMoreInteractions(msCoreConnector);

    }

    private List<BrokerInfo> buildBrokerInfos() {
        BrokerInfo brokerInfo = new BrokerInfo();
        brokerInfo.setCode("code");
        List<BrokerInfo> brokersMocked = new ArrayList<>();
        brokersMocked.add(brokerInfo);
        return brokersMocked;
    }

}
