package it.pagopa.selfcare.dashboard.connector.model.auth;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;

public interface ProductRole {

    SelfCareAuthority getSelfCareRole();

    String getProductRole();

    String getProductId();

}
