package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.backoffice.generated.openapi.v1.api.StationsApi;
import org.springframework.cloud.openfeign.FeignClient;


/**
 * PagoPa Ms Backoffice Rest Client
 */
@FeignClient(name = "${rest-client.pago-pa-backoffice-api.serviceCode}", url = "${rest-client.pago-pa-backoffice.base-url}")
public interface MsBackOfficeStationApiClient extends StationsApi {
}

