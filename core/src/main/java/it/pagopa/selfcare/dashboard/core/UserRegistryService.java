package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.user.User;

public interface UserRegistryService {

    User getUser(String externalId);
}
