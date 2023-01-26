package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.core.PnPGInstitutionService;
import it.pagopa.selfcare.dashboard.web.model.PnPGInstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.PnPGInstitutionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/{externalId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.pnPGInstitutions.api.getInstitution}")
    public PnPGInstitutionResource getPnPGInstitution(@ApiParam("${swagger.dashboard.pnPGInstitutions.model.externalId}")
                                                      @PathVariable("externalId")
                                                      String externalId) {

        log.trace("getPnPGInstitution start");
        log.debug("getPnPGInstitution externalId = {}", externalId);
        InstitutionInfo institutionInfo = pnPGInstitutionService.getPGInstitutionByExternalId(externalId);
        PnPGInstitutionResource result = PnPGInstitutionMapper.toResource(institutionInfo);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitution result = {}", result);
        log.trace("getPnPGInstitution end");

        return result;
    }

}
