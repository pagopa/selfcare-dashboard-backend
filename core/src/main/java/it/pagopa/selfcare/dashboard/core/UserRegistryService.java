package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.user.SaveUser;
import it.pagopa.selfcare.dashboard.connector.model.user.UserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserId;
import it.pagopa.selfcare.dashboard.connector.model.user.UserResource;

import java.util.UUID;

public interface UserRegistryService {

    UserResource search(String externalId);

    void updateUser(UUID id, String institutionId, UserDto userDto);

    UserResource getUserByInternalId(UUID id);

    UserId saveUser(String institutionId, SaveUser userDto);
}
