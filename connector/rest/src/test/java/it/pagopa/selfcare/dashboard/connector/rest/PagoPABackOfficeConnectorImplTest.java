package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.Broker;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokerPsp;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.Brokers;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokersPsp;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsBackOfficeChannelApiClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsBackOfficeStationApiClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.BrokerMapper;
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
import static org.mockito.ArgumentMatchers.anyInt;
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

    @Mock
    private BrokerMapper brokerMapper;

    @Test
    void getBrokersEC() {
        // given
        BrokerInfo brokerInfo = new BrokerInfo();
        brokerInfo.setCode("code");
        brokerInfo.setDescription("description");
        Brokers brokersResource = new Brokers();
        brokersResource.setBrokers(List.of(new Broker().brokerCode("code").description("description")));
        ResponseEntity<Brokers> response = ResponseEntity.ok(brokersResource);
        // when
        when(brokerMapper.fromBrokerResource(any())).thenReturn(brokerInfo);
        when(backOfficeStationApiClient._getBrokers(anyInt(), any(), anyInt(), any(), any(), any(), any()))
                .thenReturn(response);
        List<BrokerInfo> brokers = pagoPABackOfficeConnector.getBrokersEC(1, 1000);
        // then
        assertNotNull(brokers);
        assertEquals(1, brokers.size());
        assertEquals(brokers.get(0).getDescription(), brokerInfo.getDescription());
        assertEquals(brokers.get(0).getCode(), brokerInfo.getCode());
        verifyNoMoreInteractions(backOfficeStationApiClient);

    }

    @Test
    void getBrokersPSP() {
        // given
        BrokerInfo brokerInfo = new BrokerInfo();
        brokerInfo.setCode("code");
        brokerInfo.setDescription("description");
        BrokerPsp innerResource = new BrokerPsp().brokerPspCode("code").description("description");
        BrokersPsp brokersResource = new BrokersPsp();
        brokersResource.setBrokersPsp(List.of(innerResource));
        ResponseEntity<BrokersPsp> response = ResponseEntity.ok(brokersResource);
        when(brokerMapper.fromBrokerPSPResource(any())).thenReturn(brokerInfo);
        when(backOfficeChannelApiClient._getBrokersPsp(anyInt(), any(), anyInt(), any(), any(), any(), any()))
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