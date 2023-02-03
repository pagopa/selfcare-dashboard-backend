package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingPnPGInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Products;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.EnumSet;

/**
 * Ms Core Rest Client
 */
@FeignClient(name = "${rest-client.ms-core.serviceCode}", url = "${rest-client.ms-core.base-url}")
public interface MsCoreRestClient {

    @GetMapping(value = "${rest-client.ms-core.getOnBoardingInfo.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    OnBoardingPnPGInfo getOnBoardingInfo(@RequestParam(value = "institutionId", required = false) String institutionId,
                                         @RequestParam(value = "institutionExternalId", required = false) String institutionExternalId,
                                         @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states);

    @GetMapping(value = "${rest-client.ms-core.getInstitutionProducts.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    Products getInstitutionProducts(@PathVariable("id") String institutionId,
                                    @RequestParam(value = "states", required = false) EnumSet<ProductState> states);

}