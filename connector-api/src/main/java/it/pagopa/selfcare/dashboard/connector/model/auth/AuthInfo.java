package it.pagopa.selfcare.dashboard.connector.model.auth;

import java.util.Collection;
import java.util.Collections;

public interface AuthInfo {

    default String getInstitutionId() {
        return null;
    }

    default Collection<ProductRole> getProductRoles() {
        return Collections.emptyList();
    }

}
