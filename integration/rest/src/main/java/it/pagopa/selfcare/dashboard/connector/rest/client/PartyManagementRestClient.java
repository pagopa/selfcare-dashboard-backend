package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.dashboard.connector.rest.model.party_mgmt.Organization;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Party Management Rest Client
 */
@FeignClient(name = "${rest-client.party-mgmt.serviceCode}", url = "${rest-client.party-mgmt.base-url}")
public interface PartyManagementRestClient {

    @GetMapping(value = "${rest-client.party-mgmt.getOrganization.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Organization getOrganization(@PathVariable("organizationId") String organizationId);

}
