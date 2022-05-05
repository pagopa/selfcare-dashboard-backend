package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.user.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.UUID;

public interface UserService {

    User search(String externalId);

    void updateUser(UUID id, String institutionId, UserDto userDto);

    User getUserByInternalId(UUID id);

    UserId saveUser(String institutionId, SaveUser userDto);

    void deleteById(String userId);

    UserInfo findByRelationshipId(String relationshipId, EnumSet<User.Fields> fieldList);

    Collection<UserInfo> findByInstitutionId(String institutionId, UserInfo.UserInfoFilter userInfoFilter, EnumSet<User.Fields> fieldList);

}
