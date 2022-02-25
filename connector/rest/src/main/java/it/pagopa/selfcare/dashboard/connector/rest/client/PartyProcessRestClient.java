package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.rest.model.*;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingRequest;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.Set;

/**
 * Party Process Rest Client
 */
@FeignClient(name = "${rest-client.party-process.serviceCode}", url = "${rest-client.party-process.base-url}")
public interface PartyProcessRestClient {

    @GetMapping(value = "${rest-client.party-process.getInstitutionRelationships.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    RelationshipsResponse getInstitutionRelationships(@PathVariable("institutionId") String institutionId,
                                                      @RequestParam(value = "roles", required = false) EnumSet<PartyRole> roles,
                                                      @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states,
                                                      @RequestParam(value = "products", required = false) Set<String> products,
                                                      @RequestParam(value = "productRoles", required = false) Set<String> productRoles,
                                                      @RequestParam(value = "personId", required = false) String personId);

    @GetMapping(value = "${rest-client.party-process.getInstitutionProducts.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    Products getInstitutionProducts(@PathVariable("institutionId") String institutionId,
                                    @RequestParam(value = "states", required = false) EnumSet<ProductState> states);

    @GetMapping(value = "${rest-client.party-process.getOnBoardingInfo.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    OnBoardingInfo getOnBoardingInfo(@RequestParam(value = "institutionId", required = false) String institutionId,
                                     @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states);

    @GetMapping(value = "${rest-client.party-process.getRelationshipInfo.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    RelationshipInfo getRelationshipInfo(@PathVariable(value = "relationshipId") String relationshipId);

    @PostMapping(value = "${rest-client.party-process.onboardingSubdelegates.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void onboardingSubdelegates(@RequestBody OnboardingRequest request);

    @PostMapping(value = "${rest-client.party-process.onboardingOperators.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void onboardingOperators(@RequestBody OnboardingRequest request);

    @PostMapping(value = "${rest-client.party-process.suspendRelationship.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void suspendRelationship(@PathVariable("relationshipId") String relationshipId);

    @PostMapping(value = "${rest-client.party-process.activateRelationship.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void activateRelationship(@PathVariable("relationshipId") String relationshipId);

    @DeleteMapping(value = "${rest-client.party-process.deleteRelationshipById.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void deleteRelationshipById(@PathVariable("relationshipId") String relationshipId);
}
