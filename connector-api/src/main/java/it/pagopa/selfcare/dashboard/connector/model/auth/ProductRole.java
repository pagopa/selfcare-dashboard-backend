package it.pagopa.selfcare.dashboard.connector.model.auth;

import it.pagopa.selfcare.commons.base.security.Authority;

public interface ProductRole {

    Authority getSelfCareRole();

    String getProductRole();

    String getProductCode();

}
