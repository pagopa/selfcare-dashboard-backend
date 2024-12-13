package it.pagopa.selfcare.dashboard.client;

import it.pagopa.selfcare.user.generated.openapi.v1.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${rest-client.user-ms.serviceCode}", url = "${rest-client.user-ms.base-url}")
public interface UserApiRestClient extends UserApi {
}
