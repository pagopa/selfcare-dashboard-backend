package it.pagopa.selfcare.dashboard.connector.rest;

import io.github.resilience4j.retry.annotation.Retry;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.Brokers;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokersPsp;
import it.pagopa.selfcare.dashboard.connector.api.PagoPABackOfficeConnector;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsBackOfficeChannelApiClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsBackOfficeStationApiClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.BrokerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PagoPABackOfficeConnectorImpl implements PagoPABackOfficeConnector {

    private final MsBackOfficeStationApiClient backofficeStationApiClient;
    private final MsBackOfficeChannelApiClient backofficeChannelApiClient;
    private final BrokerMapper brokerMapper;
    private static final String DEFAULT_ORDER_DIRECTION = "ASC";

    public PagoPABackOfficeConnectorImpl(MsBackOfficeStationApiClient backofficeStationApiClient,
                                         MsBackOfficeChannelApiClient backofficeChannelApiClient,
                                         BrokerMapper brokerMapper) {
        this.backofficeStationApiClient = backofficeStationApiClient;
        this.backofficeChannelApiClient = backofficeChannelApiClient;
        this.brokerMapper = brokerMapper;
    }

    @Override
    @Retry(name = "retryTimeout")
    public List<BrokerInfo> getBrokersEC(int page, int limit) {
        log.trace("getBrokersEC start");
        ResponseEntity<Brokers> responseBrokersEC = backofficeStationApiClient._getBrokers(page, null, limit, null, null, null, DEFAULT_ORDER_DIRECTION);
        log.debug("getBrokersEC result = {}", responseBrokersEC.getBody());
        List<BrokerInfo> brokers = this.parseBrokersEC(responseBrokersEC.getBody());
        log.trace("getBrokersEC end");
        return brokers;
    }

    @Override
    @Retry(name = "retryTimeout")
    public List<BrokerInfo> getBrokersPSP(int page, int limit) {
        log.trace("getBrokersPSP start");
        ResponseEntity<BrokersPsp> responseBrokersPSP = backofficeChannelApiClient._getBrokersPsp(page, null, limit, null, null, null, DEFAULT_ORDER_DIRECTION);
        log.debug("getBrokersPSP result = {}", responseBrokersPSP.getBody());
        List<BrokerInfo> brokers = this.parseBrokersPSP(responseBrokersPSP.getBody());
        log.trace("getBrokersPSP end");
        return brokers;
    }

    private List<BrokerInfo> parseBrokersPSP(BrokersPsp brokersPspResource) {
        List<BrokerInfo> brokers = Collections.emptyList();
        if (brokersPspResource != null && brokersPspResource.getBrokersPsp() != null) {
            brokers = brokersPspResource.getBrokersPsp().stream()
                    .map(brokerMapper::fromBrokerPSPResource)
                    .collect(Collectors.toList());
        }
        return brokers;
    }

    private List<BrokerInfo> parseBrokersEC(Brokers brokersResource) {
        List<BrokerInfo> brokers = Collections.emptyList();
        if (brokersResource != null && brokersResource.getBrokers() != null) {
            brokers = brokersResource.getBrokers().stream()
                    .map(brokerMapper::fromBrokerResource)
                    .collect(Collectors.toList());
        }
        return brokers;
    }

}
