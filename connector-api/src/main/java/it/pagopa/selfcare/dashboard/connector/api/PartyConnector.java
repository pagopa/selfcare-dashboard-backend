package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;

import java.util.Collection;
import java.util.List;

public interface PartyConnector {

    InstitutionInfo getInstitution(String institutionId);

    Collection<InstitutionInfo> getInstitutions();

    RelationshipInfoResult getRelationshipInfo(String relationshipId);

    List<PartyProduct> getInstitutionProducts(String institutionId);

    Collection<AuthInfo> getAuthInfo(String institutionId);

    Collection<UserInfo> getUsers(String institutionId, UserInfo.UserInfoFilter userInfoFilter);

    void createUsers(String institutionId, String productId, CreateUserDto createUserDto);

    void suspend(String relationshipId);

    void activate(String relationshipId);

    void delete(String relationshipId);

}
