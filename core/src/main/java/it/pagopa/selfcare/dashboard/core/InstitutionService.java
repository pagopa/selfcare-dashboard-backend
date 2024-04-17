package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.connector.model.institution.*;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserId;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InstitutionService {

    InstitutionInfo getInstitution(String institutionId);

    Collection<InstitutionInfo> getInstitutions(String userId);

    void updateInstitutionGeographicTaxonomy(String institutionId, GeographicTaxonomyList geographicTaxonomies);

    List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId);

    List<ProductTree> getProductsTree();
    /**
     * @deprecated method has been deprecated because a new method has been implemented.
     * Remove the query from the repository
     */
    @Deprecated(forRemoval = true, since = "1.5")
    Collection<UserInfo> getInstitutionUsers(String institutionId, Optional<String> productId, Optional<SelfCareAuthority> role, Optional<Set<String>> productRoles);

    UserInfo getInstitutionUser(String institutionId, String userId);

    Collection<UserInfo> getInstitutionProductUsers(String institutionId, String productId, Optional<SelfCareAuthority> role, Optional<Set<String>> productRoles);

    UserId createUsers(String institutionId, String productId, CreateUserDto user);

    void addUserProductRoles(String institutionId, String productId, String userId, CreateUserDto dto);

    Institution updateInstitutionDescription(String institutionId, UpdateInstitutionResource updatePnPGInstitutionResource);

    Institution findInstitutionById(String institutionId);
}
