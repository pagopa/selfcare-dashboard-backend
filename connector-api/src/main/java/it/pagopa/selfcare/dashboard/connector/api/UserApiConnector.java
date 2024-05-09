package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInstitution;

import java.util.Collection;

import java.util.List;

public interface UserApiConnector {

    List<InstitutionBase> getUserInstitutions(String userId);

    User getUserById(String userId, String institutionId, List<String> fields);

    User searchByFiscalCode(String fiscalCode, String institutionId);

    void suspendUserProduct(String userId, String institutionId, String productId, String productRole);

    void activateUserProduct(String userId, String institutionId, String productId, String productRole);

    void deleteUserProduct(String userId, String institutionId, String productId, String productRole);

    UserInstitution getProducts(String institutionId, String userId);

    Boolean hasPermission(String institutionId, String permission, String productId);

    void updateUser(String userId, String institutionId, UpdateUserRequestDto userDto);

    Collection<UserInfo> getUsers(String institutionId, UserInfo.UserInfoFilter userInfoFilter, String loggedUserId);

    List<UserInstitution> retrieveFilteredUser(String userId, String institutionId, String productId);

    String createOrUpdateUserByFiscalCode(Institution institution, String productId, UserToCreate userDto, List<CreateUserDto.Role> role);

    void createOrUpdateUserByUserId(Institution institution, String productId, String userId, List<CreateUserDto.Role> role);

}
