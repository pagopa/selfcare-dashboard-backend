package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.model.institution.UpdateInstitutionResource;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipsResponse;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Products;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Ms Core Rest Client
 */
@FeignClient(name = "${rest-client.ms-core.serviceCode}", url = "${rest-client.ms-core.base-url}")
public interface MsCoreRestClient {

    @GetMapping(value = "${rest-client.ms-core.getOnBoardingInfo.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    OnBoardingInfo getOnBoardingInfo(@RequestParam(value = "institutionId", required = false) String institutionId,
                                     @RequestParam(value = "institutionExternalId", required = false) String institutionExternalId,
                                     @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states);

    @GetMapping(value = "${rest-client.ms-core.getInstitutionProducts.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    Products getInstitutionProducts(@PathVariable("id") String institutionId,
                                    @RequestParam(value = "states", required = false) EnumSet<ProductState> states);

    @GetMapping(value = "${rest-client.ms-core.getUserInstitutionRelationships.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    RelationshipsResponse getUserInstitutionRelationships(@PathVariable("id") String institutionId,
                                                          @RequestParam(value = "roles", required = false) EnumSet<PartyRole> roles,
                                                          @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states,
                                                          @RequestParam(value = "products", required = false) Set<String> productIds,
                                                          @RequestParam(value = "productRoles", required = false) Set<String> productRoles,
                                                          @RequestParam(value = "personId", required = false) String personId);

    @GetMapping(value = "${rest-client.ms-core.getRelationship.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    RelationshipInfo getRelationship(@PathVariable(value = "relationshipId") String relationshipId);

    @GetMapping(value = "${rest-client.ms-core.getInstitution.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Institution getInstitution(@PathVariable(value = "id") String id);

    @GetMapping(value = "${rest-client.ms-core.getInstitutions.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<Institution> getInstitutionsByProductAndType(@PathVariable(value = "productId") String productId, @PathVariable(value = "type") String type);

    @PutMapping(value = "${rest-client.ms-core.updateInstitutionDescription.path}")
    @ResponseBody
    Institution updateInstitutionDescription(@PathVariable(value = "id") String institutionId,
                                             @RequestBody UpdateInstitutionResource updateDto);

    @PostMapping(value = "${rest-client.ms-core.createDelegation.path}")
    @ResponseBody
    DelegationId createDelegation(@RequestBody DelegationRequest delegation);

    @PostMapping(value = "${rest-client.ms-core.updateUser.path}")
    @ResponseBody
    void updateUserById(@PathVariable(value = "id")String userId,
                        @RequestParam(value = "institutionId")String institutionId);

}