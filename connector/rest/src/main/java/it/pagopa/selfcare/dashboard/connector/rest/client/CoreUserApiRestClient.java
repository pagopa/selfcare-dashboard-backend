package it.pagopa.selfcare.dashboard.connector.rest.client;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${rest-client.ms-core-user-api.serviceCode}", url = "${rest-client.ms-core.base-url}")
public interface CoreUserApiRestClient extends it.pagopa.selfcare.core.generated.openapi.v1.api.PersonsApi {
}
