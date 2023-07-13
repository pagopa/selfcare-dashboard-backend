package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokerPspResource;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokerResource;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokersPspResource;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokersResource;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsBackOfficeChannelApiClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsBackOfficeStationApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoPABackOfficeConnectorImplTest {

    @InjectMocks
    private PagoPABackOfficeConnectorImpl pagoPABackOfficeConnector;

    @Mock
    private MsBackOfficeStationApiClient backOfficeStationApiClient;

    @Mock
    private MsBackOfficeChannelApiClient backOfficeChannelApiClient;

    @Test
    void getBrokersEC() {
        // given
        BrokerInfo brokerInfo = new BrokerInfo();
        brokerInfo.setBrokerCode("code");
        brokerInfo.setDescription("description");
        BrokersResource brokersResource = new BrokersResource();
        brokersResource.setBrokers(List.of(new BrokerResource().brokerCode("code").description("description")));
        ResponseEntity<BrokersResource> response = ResponseEntity.ok(brokersResource);
        // when
        when(backOfficeStationApiClient._getBrokersECUsingGET(any(), any(), any(), any(), any(), any()))
                .thenReturn(response);
        List<BrokerInfo> brokers = pagoPABackOfficeConnector.getBrokersEC(1, 1000);
        // then
        assertNotNull(brokers);
        assertEquals(1, brokers.size());
        assertEquals(brokers.get(0).getDescription(), brokerInfo.getDescription());
        assertEquals(brokers.get(0).getBrokerCode(), brokerInfo.getBrokerCode());
        verifyNoMoreInteractions(backOfficeStationApiClient);

    }

    @Test
    void getBrokersPSP() {
        // given
        BrokerPspResource innerResource = new BrokerPspResource().brokerPspCode("code").description("description");
        BrokersPspResource brokersResource = new BrokersPspResource();
        brokersResource.setBrokersPsp(List.of(innerResource));
        ResponseEntity<BrokersPspResource> response = ResponseEntity.ok(brokersResource);
        when(backOfficeChannelApiClient._getBrokersPspUsingGET(any(), any(), any(), any(), any(), any()))
                .thenReturn(response);
        // when
        List<BrokerInfo> brokers = pagoPABackOfficeConnector.getBrokersPSP(1, 1000);
        // then
        assertNotNull(brokers);
        assertEquals(1, brokers.size());
        assertEquals(brokers.get(0).getDescription(), innerResource.getDescription());
        verifyNoMoreInteractions(backOfficeChannelApiClient);

    }

}