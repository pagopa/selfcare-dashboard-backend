package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.user.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserV2Service {

    Collection<InstitutionBase> getInstitutions(String userId);

    void deleteUserProduct(String userId, String institutionId, String productId, String productRole);

    void activateUserProduct(String userId, String institutionId, String productId, String productRole);

    void suspendUserProduct(String userId, String institutionId, String productId, String productRole);

    User getUserById(String userId, String institutionId, List<String> fields);

    User searchUserByFiscalCode(String fiscalCode, String institutionId);

    void updateUser(String id, String institutionId, UpdateUserRequestDto userDto);

    Collection<UserInfo> getUsersByInstitutionId(String institutionId, String productId, List<String> productRoles, String loggedUserId);

    String createUsers(String institutionId, String productId, UserToCreate userToCreate);

    void addUserProductRoles(String institutionId, String productId, String userId, Set<String> productRoles, String role);
}
