package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;

import java.util.UUID;

public interface UserRegistryConnector {
    User search(String externalId);

    User getUserByInternalId(String userId);

    void updateUser(UUID id, String institutionId, MutableUserFieldsDto entity);
}
