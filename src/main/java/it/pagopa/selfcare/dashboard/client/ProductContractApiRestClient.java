package it.pagopa.selfcare.dashboard.client;

import it.pagopa.selfcare.product.generated.openapi.v1.api.ContractTemplateApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${rest-client.product-contract.serviceCode}", url = "${rest-client.product-contract.base-url}")
public interface ProductContractApiRestClient extends ContractTemplateApi {
}
