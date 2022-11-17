package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.dashboard.connector.rest.model.relationship.Relationship;
import it.pagopa.selfcare.dashboard.connector.rest.model.token.TokenInfo;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@FeignClient(name = "${rest-client.party-management.serviceCode}", url = "${rest-client.party-management.base-url}")
public interface PartyManagementRestClient {

    @GetMapping(value = "${rest-client.party-management.getRelationshipById.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    Relationship getRelationshipById(@PathVariable("relationshipId") UUID relationshipId);


    @GetMapping(value = "${rest-client.party-management.getToken.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    TokenInfo getToken(@PathVariable("tokenId") UUID tokenId);

}
