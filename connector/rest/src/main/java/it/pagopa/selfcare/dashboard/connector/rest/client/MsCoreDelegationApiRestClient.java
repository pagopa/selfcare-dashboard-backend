package it.pagopa.selfcare.dashboard.connector.rest.client;

import org.springframework.cloud.openfeign.FeignClient;
@FeignClient(name = "${rest-client.ms-core-delegation-api.serviceCode}", url = "${rest-client.ms-core.base-url}")
public interface MsCoreDelegationApiRestClient extends it.pagopa.selfcare.core.generated.openapi.v1.api.DelegationApi{
}
