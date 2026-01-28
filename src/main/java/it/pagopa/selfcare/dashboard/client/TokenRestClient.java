package it.pagopa.selfcare.dashboard.client;

import it.pagopa.selfcare.dashboard.config.restclient.OnboardingRestClientConfig;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.api.TokenControllerApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${rest-client.token-api.serviceCode}", url = "${rest-client.onboarding.base-url}", configuration = OnboardingRestClientConfig.class)
public interface TokenRestClient extends TokenControllerApi {
}
