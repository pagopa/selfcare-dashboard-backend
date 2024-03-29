package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.user.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.UUID;

public interface UserService {

    User search(String fiscalCode);

    void updateUser(UUID id, String institutionId, MutableUserFieldsDto userDto);

    User getUserByInternalId(UUID id);

    UserId saveUser(String institutionId, SaveUserDto userDto);

    void deleteById(String userId);
}
