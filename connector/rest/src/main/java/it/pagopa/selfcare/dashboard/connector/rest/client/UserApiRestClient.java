package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.user.generated.openapi.v1.api.UserControllerApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${rest-client.user-api.serviceCode}", url = "${rest-client.user-api.base-url}")
public interface UserApiRestClient extends UserControllerApi {
}
