package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;

import java.util.Collection;
import java.util.List;

public interface UserV2Service {

    Collection<InstitutionBase> getInstitutions(String userId);

    void deleteUserProduct(String userId, String institutionId, String productId);

    void activateUserProduct(String userId, String institutionId, String productId);

    void suspendUserProduct(String userId, String institutionId, String productId);

    User getUserById(String userId, String institutionId, List<String> fields);

    User searchUserByFiscalCode(String fiscalCode, String institutionId);

    void updateUser(String id, String institutionId, MutableUserFieldsDto userDto);

    Collection<UserInfo> getUsersByInstitutionId(String institutionId, String productId, String loggedUserId);
}
