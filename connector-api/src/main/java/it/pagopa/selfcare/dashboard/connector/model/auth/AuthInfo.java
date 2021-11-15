package it.pagopa.selfcare.dashboard.connector.model.auth;

import java.util.Collection;

public interface AuthInfo {

    String getRole();

    Collection<String> getProducts();

}
