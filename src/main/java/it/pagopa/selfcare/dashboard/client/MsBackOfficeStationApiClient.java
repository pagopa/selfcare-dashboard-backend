package it.pagopa.selfcare.dashboard.client;

import it.pagopa.selfcare.backoffice.generated.openapi.v1.api.CreditorInstitutionsApi;
import org.springframework.cloud.openfeign.FeignClient;


/**
 * PagoPa Ms Backoffice Rest Client
 */
@FeignClient(name = "${rest-client.pago-pa-backoffice-stations-api.serviceCode}", url = "${rest-client.pago-pa-backoffice.base-url}")
public interface MsBackOfficeStationApiClient extends CreditorInstitutionsApi {
}

