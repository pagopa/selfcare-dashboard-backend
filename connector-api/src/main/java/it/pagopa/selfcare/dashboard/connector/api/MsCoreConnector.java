package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest;
import it.pagopa.selfcare.dashboard.connector.model.institution.*;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.onboarding.OnboardingRequestInfo;

import java.util.Collection;
import java.util.List;

public interface MsCoreConnector {

    List<InstitutionInfo> getUserProducts(String userId);

    List<PartyProduct> getInstitutionProducts(String institutionId);

    Collection<AuthInfo> getAuthInfo(String institutionId);

    Institution getInstitution(String institutionId);

    Institution updateInstitutionDescription(String institutionId, UpdateInstitutionResource updatePnPGInstitutionResource);

    DelegationId createDelegation(DelegationRequest delegation);

    List<BrokerInfo> findInstitutionsByProductAndType(String productId, String type);

    List<Delegation> getDelegations(String from, String to, String productId);

    void updateUser(String userId, String institutionId);

    InstitutionInfo getOnBoardedInstitution(String institutionId);

    void updateInstitutionGeographicTaxonomy(String institutionId, GeographicTaxonomyList geographicTaxonomies);

    List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId);

    UserInfo getUser(String relationshipId);

    void createUsers(String institutionId, String productId, String userId, CreateUserDto userDto, String productTitle);

    void checkExistingRelationshipRoles(String institutionId, String productId, CreateUserDto userDto, String userId);

    void suspend(String relationshipId);

    void activate(String relationshipId);

    void delete(String relationshipId);

    OnboardingRequestInfo getOnboardingRequestInfo(String tokenId);

    void approveOnboardingRequest(String tokenId);

    void rejectOnboardingRequest(String tokenId);

    Collection<UserInfo> getUsers(String institutionId, UserInfo.UserInfoFilterV2 userInfoFilter);
}
