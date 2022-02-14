package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.user.User;

public interface UserRegistryConnector {
   User getUser(String externalId);
}
