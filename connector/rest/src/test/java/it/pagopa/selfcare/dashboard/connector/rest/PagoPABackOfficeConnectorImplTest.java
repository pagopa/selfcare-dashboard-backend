package it.pagopa.selfcare.dashboard.connector.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.Broker;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokerPsp;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.Brokers;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokersPsp;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsBackOfficeChannelApiClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsBackOfficeStationApiClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.BrokerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoPABackOfficeConnectorImplTest extends BaseConnectorTest {

    @InjectMocks
    private PagoPABackOfficeConnectorImpl pagoPABackOfficeConnector;

    @Mock
    private MsBackOfficeStationApiClient backOfficeStationApiClient;

    @Mock
    private MsBackOfficeChannelApiClient backOfficeChannelApiClient;

    @Mock
    private BrokerMapper brokerMapper;

    @BeforeEach
    public void setup() {
        super.setUp();
    }

    @Test
    void getBrokersEC() throws IOException {
        // given
        ClassPathResource resource = new ClassPathResource("stubs/BrokerInfo.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        BrokerInfo brokerInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {});
        Brokers brokersResource = new Brokers();

        Broker broker = new Broker();
        broker.setBrokerCode("code");
        broker.setDescription("description");

        brokersResource.setBrokers(List.of(new Broker().brokerCode("code").description("description")));
        ResponseEntity<Brokers> response = ResponseEntity.ok(brokersResource);
        // when
        when(brokerMapper.fromBrokerResource(broker)).thenReturn(brokerInfo);
        when(backOfficeStationApiClient._getBrokers(1, null, 1000, null, null, null, "ASC"))
                .thenReturn(response);


        List<BrokerInfo> brokers = pagoPABackOfficeConnector.getBrokersEC(1, 1000);
        // then
        assertNotNull(brokers);
        assertEquals(1, brokers.size());
        assertEquals(brokers.get(0), brokerInfo);
        verifyNoMoreInteractions(backOfficeStationApiClient);

    }

    @Test
    void getBrokersPSP() throws IOException {
        // given
        ClassPathResource resource = new ClassPathResource("stubs/BrokerInfo.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        BrokerInfo brokerInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        BrokerPsp innerResource = new BrokerPsp().brokerPspCode("code").description("description");
        BrokersPsp brokersResource = new BrokersPsp();
        brokersResource.setBrokersPsp(List.of(innerResource));
        ResponseEntity<BrokersPsp> response = ResponseEntity.ok(brokersResource);

        BrokerPsp brokerPsp = new BrokerPsp().brokerPspCode("code").description("description");

        when(brokerMapper.fromBrokerPSPResource(brokerPsp)).thenReturn(brokerInfo);
        when(backOfficeChannelApiClient._getBrokersPsp(1, null, 1000, null, null, null, "ASC"))
                .thenReturn(response);
        // when
        List<BrokerInfo> brokers = pagoPABackOfficeConnector.getBrokersPSP(1, 1000);
        // then
        assertNotNull(brokers);
        assertEquals(1, brokers.size());
        assertEquals(brokers.get(0), brokerInfo);
        verifyNoMoreInteractions(backOfficeChannelApiClient);

    }

    @Test
    void testGetBrokersEC_NullResponse() {
        // given
        ResponseEntity<Brokers> response = ResponseEntity.ok(null);
        when(backOfficeStationApiClient._getBrokers(1, null, 1000, null, null, null, "ASC"))
                .thenReturn(response);

        // when
        List<BrokerInfo> brokers = pagoPABackOfficeConnector.getBrokersEC(1, 1000);

        // then
        assertNotNull(brokers);
        assertTrue(brokers.isEmpty());
        verifyNoMoreInteractions(backOfficeStationApiClient, brokerMapper);
    }

    @Test
    void testGetBrokersPSP_EmptyResponse() {
        // given
        BrokersPsp brokersResource = new BrokersPsp();
        ResponseEntity<BrokersPsp> response = ResponseEntity.ok(brokersResource);
        when(backOfficeChannelApiClient._getBrokersPsp(1, null, 1000, null, null, null, "ASC"))
                .thenReturn(response);

        // when
        List<BrokerInfo> brokers = pagoPABackOfficeConnector.getBrokersPSP(1, 1000);

        // then
        assertNotNull(brokers);
        assertTrue(brokers.isEmpty());
        verifyNoMoreInteractions(backOfficeChannelApiClient, brokerMapper);
    }

}