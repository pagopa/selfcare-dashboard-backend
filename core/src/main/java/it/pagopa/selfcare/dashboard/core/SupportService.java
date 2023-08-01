package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.support.SupportRequest;

public interface SupportService {

    String sendRequest(SupportRequest supportRequest);
}
