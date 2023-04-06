package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.rest.model.InstitutionPut;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipsResponse;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Products;
import it.pagopa.selfcare.dashboard.connector.rest.model.relationship.Relationship;
import it.pagopa.selfcare.dashboard.connector.rest.model.token.TokenInfo;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Ms-Core Rest Client
 */
@FeignClient(name = "${rest-client.ms-core.serviceCode}", url = "${rest-client.ms-core.base-url}")
public interface MsCoreRestClient {

    @GetMapping(value = "${rest-client.ms-core.getUserInstitutionRelationships.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    RelationshipsResponse getUserInstitutionRelationships(@PathVariable("id") String institutionId,
                                                          @RequestParam(value = "roles", required = false) EnumSet<PartyRole> roles,
                                                          @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states,
                                                          @RequestParam(value = "products", required = false) Set<String> productIds,
                                                          @RequestParam(value = "productRoles", required = false) Set<String> productRoles,
                                                          @RequestParam(value = "personId", required = false) String personId);

    @GetMapping(value = "${rest-client.ms-core.getInstitutionProducts.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    Products getInstitutionProducts(@PathVariable("id") String institutionId,
                                    @RequestParam(value = "states", required = false) EnumSet<ProductState> states);

    @GetMapping(value = "${rest-client.ms-core.getOnBoardingInfo.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    OnBoardingInfo getOnBoardingInfo(@RequestParam(value = "institutionId", required = false) String institutionId,
                                     @RequestParam(value = "institutionExternalId", required = false) String institutionExternalId,
                                     @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states);

    @PutMapping(value = "${rest-client.ms-core.updateInstitution.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void updateInstitutionGeographicTaxonomy(@PathVariable("id") String institutionId,
                                             @RequestBody InstitutionPut request);


    @GetMapping(value = "${rest-client.ms-core.getRelationship.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    RelationshipInfo getRelationship(@PathVariable(value = "relationshipId") String relationshipId);

    @PostMapping(value = "${rest-client.ms-core.onboardingSubdelegates.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void onboardingSubdelegates(@RequestBody OnboardingUsersRequest request);

    @PostMapping(value = "${rest-client.ms-core.onboardingOperators.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void onboardingOperators(@RequestBody OnboardingUsersRequest request);

    @PostMapping(value = "${rest-client.ms-core.suspendRelationship.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void suspendRelationship(@PathVariable("relationshipId") String relationshipId);

    @PostMapping(value = "${rest-client.ms-core.activateRelationship.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void activateRelationship(@PathVariable("relationshipId") String relationshipId);

    @DeleteMapping(value = "${rest-client.ms-core.deleteRelationshipById.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void deleteRelationshipById(@PathVariable("relationshipId") String relationshipId);

    @GetMapping(value = "${rest-client.ms-core.getInstitution.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Institution getInstitution(@PathVariable(value = "id") String id);

    @GetMapping(value = "${rest-client.ms-core.getInstitutionByExternalId.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Institution getInstitutionByExternalId(@PathVariable(value = "externalId") String externalId);

    @PostMapping(value = "${rest-client.ms-core.approveOnboardingRequest.path}")
    @ResponseBody
    void approveOnboardingRequest(@PathVariable(value = "tokenId") String tokenId);

    @DeleteMapping(value = "${rest-client.ms-core.rejectOnboardingRequest.path}")
    @ResponseBody
    void rejectOnboardingRequest(@PathVariable(value = "tokenId") String tokenId);

    @GetMapping(value = "${rest-client.ms-core.getRelationshipById.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    Relationship getRelationshipById(@PathVariable("relationshipId") UUID relationshipId);


    @GetMapping(value = "${rest-client.ms-core.getToken.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    TokenInfo getToken(@PathVariable("tokenId") UUID tokenId);

    /*
    @GetMapping(value = "${rest-client.party-management.getInstitutionByExternalId.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Institution getInstitutionByExternalId(@PathVariable(value = "externalId") String externalId);

    */
}
