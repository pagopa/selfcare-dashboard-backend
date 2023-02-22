package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.PnPGInstitutionLegalAddressData;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.core.PnPGInstitutionService;
import it.pagopa.selfcare.dashboard.web.model.InstitutionPnPGResource;
import it.pagopa.selfcare.dashboard.web.model.PnPGInstitutionLegalAddressResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionPnPGMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.PnPGOnboardingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/pnPGInstitutions", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "pnPGInstitutions")
public class PnPGInstitutionController {

    private final PnPGInstitutionService pnPGInstitutionService;


    @Autowired
    public PnPGInstitutionController(PnPGInstitutionService pnPGInstitutionService) {
        this.pnPGInstitutionService = pnPGInstitutionService;
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.pnPGInstitutions.api.getInstitutions}")
    public List<InstitutionPnPGResource> getPnPGInstitutions() {

        log.trace("getPnPGInstitution start");
        Collection<InstitutionInfo> institutions = pnPGInstitutionService.getInstitutions();
        List<InstitutionPnPGResource> result = institutions.stream()
                .map(InstitutionPnPGMapper::toResource)
                .collect(Collectors.toList());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getPnPGInstitution result = {}", result);
        log.trace("getPnPGInstitution end");

        return result;
    }

    @GetMapping(value = "/{institutionId}/products")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionProducts}")
    public List<PartyProduct> getPnPGInstitutionProducts(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                         @PathVariable("institutionId")
                                                         String institutionId) {
        log.trace("getPnPGInstitutionProducts start");
        log.debug("getPnPGInstitutionProducts institutionId = {}", institutionId);
        List<PartyProduct> result = pnPGInstitutionService.getInstitutionProducts(institutionId);
        log.debug("getPnPGInstitutionProducts result = {}", result);
        log.trace("getPnPGInstitutionProducts end");

        return result;
    }

    @GetMapping(value = "/{externalInstitutionId}/legal-address")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.pnPGInstitutions.api.getInstitutionLegalAddress}")
    public PnPGInstitutionLegalAddressResource getInstitutionLegalAddress(@ApiParam("${swagger.dashboard.pnPGInstitutions.model.externalId}")
                                                                          @PathVariable("externalInstitutionId")
                                                                          String externalInstitutionId) {
        log.trace("getInstitutionLegalAddress start");
        log.debug("getInstitutionLegalAddress institutionId = {}", externalInstitutionId);
        PnPGInstitutionLegalAddressData institutionLegalAddressData = pnPGInstitutionService.getInstitutionLegalAddress(externalInstitutionId);
        PnPGInstitutionLegalAddressResource result = PnPGOnboardingMapper.toResource(institutionLegalAddressData);
        log.debug("getInstitutionLegalAddress result = {}", result);
        log.trace("getInstitutionLegalAddress end");
        return result;
    }

}