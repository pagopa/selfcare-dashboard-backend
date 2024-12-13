package it.pagopa.selfcare.dashboard.client;

import it.pagopa.selfcare.user.generated.openapi.v1.api.UserPermissionControllerApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${rest-client.user-permission.serviceCode}", url = "${rest-client.user-ms.base-url}")
public interface UserPermissionRestClient extends UserPermissionControllerApi {

}
