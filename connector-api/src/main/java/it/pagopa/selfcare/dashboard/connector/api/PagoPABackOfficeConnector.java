package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;

import java.util.List;

public interface PagoPABackOfficeConnector {

    List<BrokerInfo> getBrokersPSP(int page, int limit);
    List<BrokerInfo> getBrokersEC(int page, int limit);

}
