package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.core.generated.openapi.v1.api.OnboardingApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${rest-client.ms-core-onboarding-api.serviceCode}", url = "${rest-client.ms-core.base-url}")
public interface CoreOnboardingApiRestClient extends OnboardingApi {
}
