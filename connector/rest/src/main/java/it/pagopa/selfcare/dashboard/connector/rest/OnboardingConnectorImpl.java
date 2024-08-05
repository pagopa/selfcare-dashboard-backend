package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.api.OnboardingConnector;
import it.pagopa.selfcare.dashboard.connector.rest.client.OnboardingRestClient;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingGetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingConnectorImpl implements OnboardingConnector {

    private final OnboardingRestClient onboardingRestClient;

    @Override
    public Boolean getOnboardingWithFilter(String institutionId, String productId, String status) {
        ResponseEntity<OnboardingGetResponse> response = onboardingRestClient._v1OnboardingGet(null, institutionId, null, null, productId, 1, status, null, null);
        return checkOnboardingPresence(response);
    }

    private static boolean checkOnboardingPresence(ResponseEntity<OnboardingGetResponse> response) {
        return Objects.nonNull(response)
                && Objects.nonNull(response.getBody())
                && !CollectionUtils.isEmpty(response.getBody().getItems());
    }
}
