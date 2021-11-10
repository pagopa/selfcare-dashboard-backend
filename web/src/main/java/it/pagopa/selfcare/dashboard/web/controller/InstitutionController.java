package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.dashboard.core.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/institutions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "institutions")
public class InstitutionController {

    private final FileStorageService storageService;


    @Autowired
    public InstitutionController(FileStorageService storageService) {
        this.storageService = storageService;
    }


    @PutMapping(value = "/{institutionId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.saveInstitutionLogo}")
    public Object saveInstitutionLogo(@ApiParam("${swagger.dashboard.model.id}")
                                      @PathVariable("institutionId") String institutionId,
                                      @ApiParam("${swagger.dashboard.institutions.model.logo}")
                                      @RequestPart("logo") MultipartFile logo) throws IOException {
        storageService.storeInstitutionLogo(institutionId, logo.getInputStream(), logo.getContentType(), logo.getOriginalFilename());
        return null;
    }

}
