package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.core.PnPGInstitutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/v1/pnPGInstitutions", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "pnPGInstitutions")
public class PnPGInstitutionController {

    private final PnPGInstitutionService pnPGInstitutionService;


    @Autowired
    public PnPGInstitutionController(PnPGInstitutionService pnPGInstitutionService) {
        this.pnPGInstitutionService = pnPGInstitutionService;
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

}
