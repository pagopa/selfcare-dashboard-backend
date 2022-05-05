package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.SaveUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserId;

import java.util.EnumSet;
import java.util.UUID;

public interface UserRegistryConnector {

    User search(String externalId, EnumSet<User.Fields> fieldList);

    User getUserByInternalId(String userId, EnumSet<User.Fields> fieldList);

    void updateUser(UUID id, MutableUserFieldsDto entity);

    UserId saveUser(SaveUserDto entity);

    void deleteById(String userId);

}
