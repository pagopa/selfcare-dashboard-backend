package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.support.SupportRequest;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportResponse;

public interface SupportService {
    SupportResponse sendRequest(SupportRequest supportRequest);
}
