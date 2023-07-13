package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;

import java.util.List;

public interface BrokerService {

    List<BrokerInfo> findAllByInstitutionType(String institutionType);

}
