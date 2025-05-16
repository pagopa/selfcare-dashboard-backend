package it.pagopa.selfcare.dashboard.service;

import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingsResponse;
import it.pagopa.selfcare.dashboard.model.institution.Institution;
import it.pagopa.selfcare.dashboard.model.user.UserInfo;
import org.springframework.core.io.Resource;

import java.util.List;

public interface InstitutionV2Service {

    UserInfo getInstitutionUser(String institutionId, String userId, String loggedUserId);

    Institution findInstitutionById(String institutionId);

    Boolean verifyIfExistsPendingOnboarding(String taxCode, String subunitCode, String productId);
    OnboardingsResponse getOnboardingsInfoResponse(String institutionId, List<String> products);
    Resource getContract(String institutionId, String productId);
}
