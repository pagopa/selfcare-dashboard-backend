package it.pagopa.selfcare.dashboard.client;

import it.pagopa.selfcare.core.generated.openapi.v1.api.InstitutionApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${rest-client.ms-core-institution-api.serviceCode}", url = "${rest-client.ms-core.base-url}")
public interface CoreInstitutionApiRestClient extends InstitutionApi {
}
