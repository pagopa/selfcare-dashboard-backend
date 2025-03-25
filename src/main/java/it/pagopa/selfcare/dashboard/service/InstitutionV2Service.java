package it.pagopa.selfcare.dashboard.service;

import it.pagopa.selfcare.dashboard.model.institution.Institution;
import it.pagopa.selfcare.dashboard.model.user.UserInfo;

public interface InstitutionV2Service {

    UserInfo getInstitutionUser(String institutionId, String userId, String loggedUserId);

    Institution findInstitutionById(String institutionId);

    Boolean verifyIfExistsPendingOnboarding(String taxCode, String subunitCode, String productId);
}
