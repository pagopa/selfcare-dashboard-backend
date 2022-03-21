package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserDto;

import java.util.UUID;

public interface UserRegistryConnector {
    User getUserByExternalId(String externalId);

    User getUserByInternalId(String userId);

    void saveUser(UUID id, String institutionId, UserDto entity);
}
