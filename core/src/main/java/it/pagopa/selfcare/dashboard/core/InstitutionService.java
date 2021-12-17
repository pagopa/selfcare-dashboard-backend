package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InstitutionService {

    InstitutionInfo getInstitution(String institutionId);

    Collection<InstitutionInfo> getInstitutions();

    List<Product> getInstitutionProducts(String institutionId);

    Collection<UserInfo> getInstitutionUsers(String institutionId, Optional<String> productId, Optional<SelfCareAuthority> role);

    Collection<UserInfo> getInstitutionProductUsers(String institutionId, String productId, Optional<SelfCareAuthority> role);

    void createUsers(String institutionId, String productId, CreateUserDto user);

}