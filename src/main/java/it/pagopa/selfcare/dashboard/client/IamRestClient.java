package it.pagopa.selfcare.dashboard.client;

import it.pagopa.selfcare.iam.generated.openapi.v1.api.IamApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${rest-client.iam.serviceCode}", url = "${rest-client.iam.base-url}")
public interface IamRestClient extends IamApi {
}
