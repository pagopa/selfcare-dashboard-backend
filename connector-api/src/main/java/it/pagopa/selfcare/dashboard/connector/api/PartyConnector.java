package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PartyConnector {

    InstitutionInfo getInstitution(String institutionId);

    Collection<InstitutionInfo> getInstitutions();

    List<String> getInstitutionProducts(String institutionId);

    Collection<AuthInfo> getAuthInfo(String institutionId);

    Collection<UserInfo> getUsers(String institutionId, Optional<SelfCareAuthority> role, Optional<Set<String>> productIds);

    void createUsers(String institutionId, String productId, CreateUserDto createUserDto);

}
