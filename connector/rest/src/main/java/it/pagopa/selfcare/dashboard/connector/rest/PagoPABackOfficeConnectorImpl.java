package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokerPspResource;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokerResource;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokersPspResource;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokersResource;
import it.pagopa.selfcare.dashboard.connector.api.PagoPABackOfficeConnector;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsBackOfficeChannelApiClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsBackOfficeStationApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PagoPABackOfficeConnectorImpl implements PagoPABackOfficeConnector {

    private final MsBackOfficeStationApiClient backofficeStationApiClient;
    private final MsBackOfficeChannelApiClient backofficeChannelApiClient;
    private static final String DEFAULT_ORDER_DIRECTION = "ASC";
    private static final String DEFAULT_ORDER_BY = "description";

    public PagoPABackOfficeConnectorImpl(MsBackOfficeStationApiClient backofficeStationApiClient,
                                         MsBackOfficeChannelApiClient backofficeChannelApiClient) {
        this.backofficeStationApiClient = backofficeStationApiClient;
        this.backofficeChannelApiClient = backofficeChannelApiClient;
    }

    protected static final Function<BrokerResource, BrokerInfo> BACKOFFICE_BROKER_DATA_TO_BROKER_INFO_FUNCTION = brokerData -> {
        BrokerInfo brokerInfo = new BrokerInfo();
        brokerInfo.setBrokerCode(brokerData.getBrokerCode());
        brokerInfo.setEnabled(brokerData.getEnabled());
        brokerInfo.setExtendedFaultBean(brokerData.getExtendedFaultBean());
        brokerInfo.setDescription(brokerData.getDescription());
        return brokerInfo;
    };

    protected static final Function<BrokerPspResource, BrokerInfo> BACKOFFICE_BROKER_PSP_DATA_TO_BROKER_PSP_INFO_FUNCTION = brokerData -> {
        BrokerInfo brokerInfo = new BrokerInfo();
        brokerInfo.setBrokerPspCode(brokerData.getBrokerPspCode());
        brokerInfo.setEnabled(brokerData.getEnabled());
        brokerInfo.setDescription(brokerData.getDescription());
        return brokerInfo;
    };

    @Override
    public List<BrokerInfo> getBrokersEC(int page, int limit) {
        log.trace("getBrokersEC start");
        ResponseEntity<BrokersResource> responseBrokersEC = backofficeStationApiClient._getBrokersECUsingGET(page, limit, null, null, DEFAULT_ORDER_BY,DEFAULT_ORDER_DIRECTION);
        log.debug("getBrokersEC result = {}", responseBrokersEC.getBody());
        List<BrokerInfo> brokers = this.parseBrokersEC(responseBrokersEC.getBody());
        log.trace("getBrokersEC end");
        return brokers;
    }

    @Override
    public List<BrokerInfo> getBrokersPSP(int page, int limit) {
        log.trace("getBrokersPSP start");
        ResponseEntity<BrokersPspResource> responseBrokersPSP = backofficeChannelApiClient._getBrokersPspUsingGET(page, limit, null, null, DEFAULT_ORDER_BY,DEFAULT_ORDER_DIRECTION);
        log.debug("getBrokersPSP result = {}", responseBrokersPSP.getBody());
        List<BrokerInfo> brokers = this.parseBrokersPSP(responseBrokersPSP.getBody());
        log.trace("getBrokersPSP end");
        return brokers;
    }

    protected List<BrokerInfo> parseBrokersPSP(BrokersPspResource brokersPspResource) {
        log.trace("parseBrokersEC start");
        log.debug("parseBrokersEC backOfficeBrokerData = {}", brokersPspResource);
        List<BrokerInfo> brokers = Collections.emptyList();
        if (brokersPspResource != null && brokersPspResource.getBrokersPsp() != null) {
            brokers = brokersPspResource.getBrokersPsp().stream()
                    .map(BACKOFFICE_BROKER_PSP_DATA_TO_BROKER_PSP_INFO_FUNCTION)
                    .collect(Collectors.toList());
        }
        log.debug("parseBrokersEC result = {}", brokers);
        log.trace("parseBrokersEC end");
        return brokers;
    }

    protected List<BrokerInfo> parseBrokersEC(BrokersResource brokersResource) {
        log.trace("parseBrokersEC start");
        log.debug("parseBrokersEC backOfficeBrokerData = {}", brokersResource);
        List<BrokerInfo> brokers = Collections.emptyList();
        if (brokersResource != null && brokersResource.getBrokers() != null) {
            brokers = brokersResource.getBrokers().stream()
                    .map(BACKOFFICE_BROKER_DATA_TO_BROKER_INFO_FUNCTION)
                    .collect(Collectors.toList());
        }
        log.debug("parseBrokersEC result = {}", brokers);
        log.trace("parseBrokersEC end");
        return brokers;
    }

}
