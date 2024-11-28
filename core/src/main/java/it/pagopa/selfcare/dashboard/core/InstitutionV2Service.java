package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;

public interface InstitutionV2Service {

    UserInfo getInstitutionUser(String institutionId, String userId, String loggedUserId);

    Institution findInstitutionById(String institutionId);

    Boolean verifyIfExistsPendingOnboarding(String taxCode, String subunitCode, String productId);
}
