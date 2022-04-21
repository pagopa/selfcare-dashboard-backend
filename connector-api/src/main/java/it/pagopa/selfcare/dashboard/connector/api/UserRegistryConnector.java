package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserResource;

import java.util.UUID;

public interface UserRegistryConnector {
    UserResource search(String externalId);

    UserResource getUserByInternalId(String userId);

    void updateUser(UUID id, String institutionId, MutableUserFieldsDto entity);
}
