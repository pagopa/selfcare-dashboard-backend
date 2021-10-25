package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Citizen Rest Client
 */
@FeignClient(name = "${rest-client.party.serviceCode}", url = "${rest-client.party.base-url}")
public interface PartyRestClient {

    @GetMapping(value = "${rest-client.party.getInstitutionRelationships.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    RelationshipsResponse getInstitutionRelationships(@PathVariable("institutionId") String institutionId);

}
