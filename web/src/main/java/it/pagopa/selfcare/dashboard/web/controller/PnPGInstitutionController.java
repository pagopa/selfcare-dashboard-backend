package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.core.PnPGInstitutionService;
import it.pagopa.selfcare.dashboard.web.model.PnPGInstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.UpdateInstitutionDto;
import it.pagopa.selfcare.dashboard.web.model.mapper.PnPGInstitutionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public List<PnPGInstitutionResource> getPnPGInstitutions() {

        log.trace("getPnPGInstitution start");
        Collection<InstitutionInfo> institutions = pnPGInstitutionService.getInstitutions();
        List<PnPGInstitutionResource> result = institutions.stream()
                .map(PnPGInstitutionMapper::toResource)
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

    @PutMapping(value = "/{institutionId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.pnPGInstitutions.api.updateInstitutionDescription}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ADMIN')")
    public Institution updateInstitutionDescription(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                    @PathVariable("institutionId")
                                                    String institutionId,
                                                    @RequestBody
                                                    @Valid
                                                    UpdateInstitutionDto institutionDto) {
        log.trace("updateInstitutionDescription start");
        log.debug("updateInstitutionDescription institutionId = {}, institutionDto{}", institutionId, institutionDto);
        Institution result = pnPGInstitutionService.updateInstitutionDescription(institutionId, PnPGInstitutionMapper.toUpdateResource(institutionDto));
        log.debug("updateInstitutionDescription result = {}", result);
        log.trace("updateInstitutionDescription end");
        return result;
    }

}
