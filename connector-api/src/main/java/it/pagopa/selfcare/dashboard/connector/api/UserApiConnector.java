package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;

import java.util.Collection;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;

import java.util.List;

public interface UserApiConnector {

    List<InstitutionBase> getUserInstitutions(String userId);

    User getUserById(String userId);

    User searchByFiscalCode(String fiscalCode);

    void suspendUserProduct(String userId, String institutionId, String productId);

    void activateUserProduct(String userId, String institutionId, String productId);

    void deleteUserProduct(String userId, String institutionId, String productId);

    Boolean hasPermission(String institutionId, String permission, String productId);

    void updateUser(String userId, String institutionId, MutableUserFieldsDto userDto);

    Collection<UserInfo> getUsers(String institutionId, UserInfo.UserInfoFilter userInfoFilter, String loggedUserId);
}
