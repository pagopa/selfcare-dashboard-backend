package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Ms Core Rest Client
 */
@FeignClient(name = "${rest-client.ms-core.serviceCode}", url = "${rest-client.ms-core.base-url}")
public interface MsCoreRestClient {

    @GetMapping(value = "${rest-client.ms-core.getPGInstitutionByExternalId.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    InstitutionInfo getPGInstitutionByExternalId(@PathVariable(value = "externalId") String externalId);

}