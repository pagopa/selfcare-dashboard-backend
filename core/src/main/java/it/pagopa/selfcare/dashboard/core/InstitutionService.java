package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InstitutionService {

    InstitutionInfo getInstitution(String institutionId);

    Collection<InstitutionInfo> getInstitutions();

    List<Product> getInstitutionProducts(String institutionId);

    Collection<UserInfo> getUsers(String institutionId, Optional<SelfCareAuthority> role);

    Collection<UserInfo> getUsers(String institutionId, Optional<SelfCareAuthority> role, Set<String> productIds);

    void createUsers(String institutionId, String productId, CreateUserDto user);

}
