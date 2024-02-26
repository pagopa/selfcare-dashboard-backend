package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;

import java.util.List;


public interface UserApiConnector {

    List<InstitutionInfo> getUserProducts(String userId);

    void suspendUserProduct(String userId, String institutionId, String productId);

    void activateUserProduct(String userId, String institutionId, String productId);

    void deleteUserProduct(String userId, String institutionId, String productId);
}
