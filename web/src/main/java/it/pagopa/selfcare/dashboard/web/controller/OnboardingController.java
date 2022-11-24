package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.dashboard.connector.onboarding.OnboardingRequestInfo;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.web.model.mapper.OnboardingRequestMapper;
import it.pagopa.selfcare.dashboard.web.model.onboarding.OnboardingRequestResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/onboarding-requests", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "onboarding")
public class OnboardingController {

    private final InstitutionService institutionService;


    @Autowired
    public OnboardingController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }


    @GetMapping(value = "/{tokenId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.onboarding-requests.api.retrieveOnboardingRequest}")
    public OnboardingRequestResource retrieveOnboardingRequest(@ApiParam("${swagger.dashboard.onboarding-requests.model.tokenId}")
                                                               @PathVariable("tokenId")
                                                               UUID tokenId) {
        log.trace("retrieveOnboardingRequest start");
        log.debug("retrieveOnboardingRequest tokenId = {}", tokenId);
        final OnboardingRequestInfo onboardingRequestInfo = institutionService.getOnboardingRequestInfo(tokenId.toString());
        OnboardingRequestResource result = OnboardingRequestMapper.toResource(onboardingRequestInfo);
        log.debug("retrieveOnboardingRequest result = {}", result);
        log.trace("retrieveOnboardingRequest end");
        return result;
    }

    @PostMapping(value = "/approve/{tokenId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.onboarding-requests.api.approveOnboardingRequest}")
    public void approveOnboardingRequest(@ApiParam("${swagger.dashboard.onboarding-requests.model.tokenId}")
                                         @PathVariable("tokenId")
                                         UUID tokenId) {
        log.trace("approveOnboardingRequest start");
        log.debug("approveOnboardingRequest tokenId = {}", tokenId);
        institutionService.approveOnboardingRequest(tokenId.toString());
        log.trace("approveOnboardingRequest end");
    }
}
