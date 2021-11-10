package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.RelationshipsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Party Process Rest Client
 */
@FeignClient(name = "${rest-client.party-process.serviceCode}", url = "${rest-client.party-process.base-url}")
public interface PartyProcessRestClient extends PartyConnector {

    @GetMapping(value = "${rest-client.party-process.getInstitutionRelationships.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    RelationshipsResponse getInstitutionRelationships(@PathVariable("institutionId") String institutionId);

    @GetMapping(value = "${rest-client.party-process.getOnBoardingInfo.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    OnBoardingInfo getOnBoardingInfo(@RequestParam(value = "institutionId", required = false) String institutionId);

}
