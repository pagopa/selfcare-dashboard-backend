package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.dashboard.connector.model.institution.PnPGInstitutionLegalAddressData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Party Registry Proxy Rest Client
 */
@FeignClient(name = "${rest-client.party-registry-proxy.serviceCode}", url = "${rest-client.party-registry-proxy.base-url}")
public interface PartyRegistryProxyRestClient {

    @GetMapping(value = "${rest-client.party-registry-proxy.getInstitutionLegalAddress.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    PnPGInstitutionLegalAddressData getInstitutionLegalAddress(@RequestParam(value = "taxId") String externalInstitutionId);

}
