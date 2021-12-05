package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.core.FileStorageService;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.ProductsResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.ProductsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/institutions", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "institutions")
public class InstitutionController {

    private final FileStorageService storageService;
    private final InstitutionService institutionService;


    @Autowired
    public InstitutionController(FileStorageService storageService, InstitutionService institutionService) {
        this.storageService = storageService;
        this.institutionService = institutionService;
    }


    @PutMapping(value = "/{institutionId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.saveInstitutionLogo}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', null)")
    public Object saveInstitutionLogo(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                      @PathVariable("institutionId") String institutionId,
                                      @ApiParam("${swagger.dashboard.institutions.model.logo}")
                                      @RequestPart("logo") MultipartFile logo) throws IOException {
        if (log.isDebugEnabled()) {
            log.trace("InstitutionController.saveInstitutionLogo");
            log.debug("institutionId = " + institutionId + ", logo = " + logo);
        }
        storageService.storeInstitutionLogo(institutionId, logo.getInputStream(), logo.getContentType(), logo.getOriginalFilename());
        return null;
    }


    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutions}")
    public List<InstitutionResource> getInstitutions() {
        if (log.isTraceEnabled()) {
            log.trace("InstitutionController.getInstitutions");
        }
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions();
        return institutions.stream()
                .map(InstitutionMapper::toResource)
                .collect(Collectors.toList());
    }


    @GetMapping("/{institutionId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitution}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', null)")
    public InstitutionResource getInstitution(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                              @PathVariable("institutionId")
                                                      String institutionId) {
        if (log.isDebugEnabled()) {
            log.trace("InstitutionController.getInstitution");
            log.debug("institutionId = " + institutionId);
        }
        InstitutionInfo institutionInfo = institutionService.getInstitution(institutionId);
        return InstitutionMapper.toResource(institutionInfo);
    }


    @GetMapping(value = "/{institutionId}/products")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionProducts}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', null)")
    public List<ProductsResource> getInstitutionProducts(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                         @PathVariable("institutionId")
                                                                 String institutionId) {
        if (log.isTraceEnabled()) {
            log.trace("InstitutionController.getInstitutionProducts");
        }
        List<Product> products = institutionService.getInstitutionProducts(institutionId);
        return products.stream()
                .map(ProductsMapper::toResource)
                .collect(Collectors.toList());
    }

}
