package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserDto;

import java.util.UUID;

public interface UserRegistryService {

    User search(String externalId);

    void updateUser(UUID id, String institutionId, UserDto userDto);
}
