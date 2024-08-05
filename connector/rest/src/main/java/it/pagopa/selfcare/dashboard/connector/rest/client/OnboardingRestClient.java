package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.onboarding.generated.openapi.v1.api.OnboardingControllerApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${rest-client.onboarding-api.serviceCode}", url = "${rest-client.onboarding.base-url}")
public interface OnboardingRestClient extends OnboardingControllerApi {
}
