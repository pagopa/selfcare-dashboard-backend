package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomyList;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.onboarding.OnboardingRequestInfo;

import java.util.Collection;
import java.util.List;

public interface PartyConnector {

    InstitutionInfo getOnBoardedInstitution(String institutionId);

    Collection<InstitutionInfo> getOnBoardedInstitutions();

    void updateInstitutionGeographicTaxonomy(String institutionId, GeographicTaxonomyList geographicTaxonomies);

    List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId);

    UserInfo getUser(String relationshipId);

    List<PartyProduct> getInstitutionProducts(String institutionId);

    Collection<AuthInfo> getAuthInfo(String institutionId);

    Collection<UserInfo> getUsers(String institutionId, UserInfo.UserInfoFilter userInfoFilter);

    void createUsers(String institutionId, String productId, String userId, CreateUserDto userDto, String productTitle);

    void checkExistingRelationshipRoles(String institutionId, String productId, CreateUserDto userDto, String userId);

    void suspend(String relationshipId);

    void activate(String relationshipId);

    void delete(String relationshipId);

    Institution getInstitution(String institutionId);

    Institution getInstitutionByExternalId(String institutionExternalId);

    OnboardingRequestInfo getOnboardingRequestInfo(String tokenId);

    void approveOnboardingRequest(String tokenId);

    void rejectOnboardingRequest(String tokenId);
}
