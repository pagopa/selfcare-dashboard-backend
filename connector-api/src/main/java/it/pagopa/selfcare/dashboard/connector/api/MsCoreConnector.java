package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.UpdateInstitutionResource;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;

import java.util.Collection;
import java.util.List;

public interface MsCoreConnector {

    @Deprecated
    Collection<InstitutionInfo> getOnBoardedInstitutions();

    List<InstitutionInfo> getUserProducts(String userId);

    UserInfo getUser(String relationshipId);

    List<PartyProduct> getInstitutionProducts(String institutionId);

    Collection<AuthInfo> getAuthInfo(String institutionId);

    Collection<UserInfo> getUsers(String institutionId, UserInfo.UserInfoFilter userInfoFilter);

    Institution getInstitution(String institutionId);

    Institution updateInstitutionDescription(String institutionId, UpdateInstitutionResource updatePnPGInstitutionResource);

    DelegationId createDelegation(DelegationRequest delegation);

    List<BrokerInfo> findInstitutionsByProductAndType(String productId, String type);

    List<Delegation> getDelegations(String from, String to, String productId);

    void updateUser(String userId, String institutionId);

}
